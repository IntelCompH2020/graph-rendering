import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import sigma from "sigma";
import Graph from "graphology";
import { NodeDisplayData } from 'sigma/types';
import { GraphService } from '@app/core/services/http/graph.service';
import { debounceTime, distinctUntilChanged, filter, map, mergeMap, take, takeUntil, tap, switchMap } from 'rxjs/operators';
import { Subject, Observable, of } from "rxjs";
import { GraphData, GraphData as GraphEntity } from "@app/core/model/graph/graph-data.mode";
import { GraphDataLookup } from '@app/core/query/graph-data.lookup';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { EdgeData } from '@app/core/model/graph/edge-data.model';
import { NodeData } from '@app/core/model/graph/node-data.model';
import { CompareType } from '@app/core/enum/compare-type.enum';
import { ActivatedRoute, Router } from '@angular/router';
import { GraphInfoLookup } from '@app/core/model/graph/graph-info.lookup';
import { GraphPickerDialogComponent, GraphPickerDialogData, GraphPickerDialogResponse } from './graph-picker-dialog/graph-picker-dialog.component';
import { InstallationConfigurationService } from '@common/installation-configuration/installation-configuration.service';
interface State {
  hoveredNode?: string;
  searchQuery: string;

  // State derived from query:
  selectedNode?: string;
  suggestions?: Set<string>;

  // State derived from hovered node:
  hoveredNeighbors?: Set<string>;
}

@Component({
	selector: "app-graphs",
	templateUrl: "./graphs.component.html",
	styleUrls: ["./graphs.component.scss"],
})
export class GraphComponent extends BaseComponent implements OnInit {

	private rerender: sigma;
	private state: State = { searchQuery: "" };
	private graph: Graph = new Graph();
	private requestDelay = 1000;

	private static readonly DEFAULT_ZOOM_FACTOR = 1;

	public pageSize = 5000;
	public initialLoading = false;
	public isDevelopment = !this.installationConfigurationService.isProduction;

	nodeIds: Guid[]; // todo
	edgeIds : Guid[];

	_viewChanged = new Subject<any>();
	_customBBox = null;
	constructor(
		private installationConfigurationService: InstallationConfigurationService,
		private graphService: GraphService,
		private dialog: MatDialog,
		private language: TranslateService,
		private route: ActivatedRoute,
		private router: Router
	) {
		super();
	}

	ngOnInit(): void {
		this._viewChanged
			.pipe(
				debounceTime(this.requestDelay),
				distinctUntilChanged(),
				switchMap((query) => this.getGraphData(query))
			)
			.pipe(takeUntil(this._destroyed))
			.subscribe((items) => this.reloadGraph(items));

		this.rerender = new sigma(
			this.graph,
			document.getElementById("sigma-container"),
			{
				renderEdgeLabels: true,
				hideEdgesOnMove: true,

			}
		);


		this.registerQueryParamsListener();

		const nodeEvents = [
			"enterNode",
			"leaveNode",
			"downNode",
			"clickNode",
			"rightClickNode",
			"doubleClickNode",
			"wheelNode",
		] as const;
		const edgeEvents = [
			"downEdge",
			"clickEdge",
			"rightClickEdge",
			"doubleClickEdge",
			"wheelEdge",
		] as const;
		const stageEvents = [
			"downStage",
			"clickStage",
			"doubleClickStage",
			"wheelStage",
		] as const;

		stageEvents.forEach((eventType) => {
			this.rerender.on(eventType, ({ event }) => {
				this._viewChanged.next(this.rerender.viewRectangle());
			});
		});

		//Bind graph interactions:
		this.rerender.on("enterNode", ({ node }) => {
			this.setHoveredNode(node);
		});
		this.rerender.on("enterNode", ({ node }) => {
			this.setHoveredNode(node);
		});
		this.rerender.on("leaveNode", () => {
			this.setHoveredNode(undefined);
		});
		this.rerender.setSetting("nodeReducer", (node, data) => {
			const res: Partial<NodeDisplayData> = { ...data };

			if (
				this.state.hoveredNeighbors &&
				!this.state.hoveredNeighbors.has(node) &&
				this.state.hoveredNode !== node
			) {
				res.label = "";
				res.color = "#f6f6f6";
			}

			if (this.state.selectedNode === node) {
				res.highlighted = true;
			} else if (
				this.state.suggestions &&
				!this.state.suggestions.has(node)
			) {
				res.label = "";
				res.color = "#f6f6f6";
			}

			return res;
		});
	}

	setHoveredNode(node?: string) {
		if (node) {
			this.state.hoveredNode = node;
			this.state.hoveredNeighbors = new Set(this.graph.neighbors(node));
		} else {
			this.state.hoveredNode = undefined;
			this.state.hoveredNeighbors = undefined;
		}

		// Refresh rendering:
		this.rerender.refresh();
	}


	public zoomIn():void{
		const camera = this.rerender.getCamera();
		camera?.animatedZoom({
			duration: 600
		});
		this._viewChanged.next({});
	}

	public zoomOut():void{
		const camera = this.rerender.getCamera();
		camera?.animatedUnzoom({
			duration: 600
		});
		this._viewChanged.next({});

	}

	public resetZoom():void{
		const camera = this.rerender.getCamera();
		camera?.animatedReset({
			duration: 600
		});
		this._viewChanged.next({});
	}

	public onThresholdRangeChange(stringValue: string){
		try{
			const numberValue = Number.parseFloat(stringValue);
			this.rerender.setSetting("labelRenderedSizeThreshold", numberValue);
		}catch{
			console.warn('Could not get value');
		}
	}


	private fetchInitGraphData(graphInfoLookup:GraphInfoLookup ): void{
		const { nodeIds, edgeIds } = graphInfoLookup;
		this.graphService
			.getGraphInfo({
				nodeIds,
				edgeIds
			})
			.pipe(takeUntil(this._destroyed))
			.subscribe((item) => {
				this.rerender.setCustomBBox({
					x: [item.x1, item.x2],
					y: [item.y1, item.y2],
				});
				this._viewChanged.next(null);
			});
	}


	private registerQueryParamsListener():void{

		this.route.queryParams.pipe(
			take(1), // TODO POSSIBLY REMOVE IF NOT INITIALIZE ONLY ONCE
			mergeMap(params =>{ //ensure graph params

				const {edgeIds, nodeIds } = params as GraphInfoLookup;

				if (!edgeIds || !nodeIds){
					return this.graphService
						.query(GraphService.DefaultGraphListingLookup()) // query available graphs
						.pipe(
							mergeMap(// ask user to pick one
								graphs => this.dialog
									.open<GraphPickerDialogComponent, GraphPickerDialogData, GraphPickerDialogResponse>(
										GraphPickerDialogComponent,
										{
											closeOnNavigation: true,
											disableClose: true,
											minWidth: '20rem',
											data:{
												items: graphs.items?.map(graph =>({
													name: graph.name,
													graphInfo:{
														edgeIds: graph.graphEdges?.map(graphEdge => graphEdge.edge?.id).filter(x => !!x),
														nodeIds: graph.graphNodes?.map(graphNode => graphNode.node?.id).filter(x => !!x)
													}
												}))
											}
										}
									)
									.afterClosed()
									.pipe(
										tap(x => !x && this.router.navigate(['/'])),
										filter(x => !!x),
									)
							)
						)
				}
				return of({
					edgeIds,
					nodeIds
				})
			}),
			takeUntil(this._destroyed),
		)
		.subscribe(params => {
			let { edgeIds, nodeIds } = params;


			if(!edgeIds || !nodeIds){
				console.warn('No params were passed');
				return;
			}
			//  got params from outside the app
			if(!Array.isArray(edgeIds)){
				edgeIds = [Guid.parse((edgeIds as any).toString())];
			}
			// got query params from outside the app
			if(!Array.isArray(nodeIds)){
				nodeIds = [Guid.parse((nodeIds as any).toString())];
			}
			this.nodeIds = nodeIds;
			this.edgeIds = edgeIds;
			this.initialLoading = true;
			this.fetchInitGraphData({ edgeIds, nodeIds });
		})
	}



	private getGraphData(viewRectangle: any): Observable<GraphEntity> {
		const lookup: GraphDataLookup = new GraphDataLookup();
		lookup.nodeIds = this.nodeIds;
		lookup.edgeIds = this.edgeIds;
		lookup.project = {
			fields: [
				// nameof<GraphData>(x => x.size),
				nameof<GraphData>(x => x.edges) + "." + nameof<EdgeData>(x => x.sourceId),
				nameof<GraphData>(x => x.edges) + "." + nameof<EdgeData>(x => x.targetId),
				nameof<GraphData>(x => x.edges) + "." + nameof<EdgeData>(x => x.weight),
				nameof<GraphData>(x => x.nodes) + "." + nameof<NodeData>(x => x.id),
				nameof<GraphData>(x => x.nodes) + "." + nameof<NodeData>(x => x.name),
				nameof<GraphData>(x => x.nodes) + "." + nameof<NodeData>(x => x.x),
				nameof<GraphData>(x => x.nodes) + "." + nameof<NodeData>(x => x.y),
			]
		};
		lookup.order = { items: [nameof<NodeData>(x => x.name)] }; //TODO
		lookup.page = { offset: 0, size: this.pageSize };

		if (viewRectangle != null) {
			viewRectangle = this.rerender.viewRectangle();
			const c1 = this.rerender.viewportToGraph(
				this.rerender.framedGraphToViewport({
					x: viewRectangle.x1,
					y: viewRectangle.y1,
				})
			);
			const c2 = this.rerender.viewportToGraph(
				this.rerender.framedGraphToViewport({
					x: viewRectangle.x2,
					y: viewRectangle.y2 - viewRectangle.height,
				})
			);
			const xMargin = (c2.x - c1.x) * 0.1;
			const yMargin = (c2.y - c1.y) * 0.1;
			lookup.x = [
				{
					compareType: CompareType.GREATER_EQUAL,
					value: c1?.x - xMargin
				},
				{
					compareType: CompareType.LESS_EQUAL,
					value: c2?.x + xMargin
				}
			];
			lookup.y = [
				{
					compareType: CompareType.GREATER_EQUAL,
					value: c2?.y + yMargin,
				},
				{
					compareType: CompareType.LESS_EQUAL,
					value: c1?.y - yMargin,
				}
			];
		}

		return this.graphService.queryData(lookup);
	}

	private reloadGraph(data: GraphEntity) {
		const RED = "#FA4F40";
		const BLUE = "#727EE0";
		const GREEN = "#5DB346";

		this.initialLoading = false;

		this.graph.clear();
		data.nodes?.forEach((element) => {
			if (!this.graph.hasNode(element.id)) {
				this.graph.addNode(element.id, {
					label: element.name,
					x: element.x,
					y: element.y,
				});
			}
		});
		data.edges?.forEach((element2) => {
			if (
				this.graph.hasNode(element2.sourceId) &&
				this.graph.hasNode(element2.targetId) &&
				!this.graph.hasEdge(element2.sourceId, element2.targetId)
			) {
				this.graph.addEdge(element2.sourceId, element2.targetId, {
					size: element2.weight * 1,
					type: "line",
				});
			}
		});
		this.rerender.refresh();
	}
}
