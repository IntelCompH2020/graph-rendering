import { Component, OnInit } from '@angular/core';
import { GraphInfoLookup } from '@app/core/model/graph/graph-info.lookup';
import { Graph, GraphEdge, GraphNode } from '@app/core/model/graph/graph.model';
import { GraphLookup } from '@app/core/query/graph.lookup';
import { GraphService } from '@app/core/services/http/graph.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { takeUntil } from 'rxjs/operators';
@Component({
	selector: 'app-home',
	templateUrl: './home.component.html',
	styleUrls: ['./home.component.scss'],
	providers:[GraphService]
})
export class HomeComponent extends BaseComponent implements OnInit{

	availableGraphs: GraphInfoLookup[] = new Array(2).fill(0).map(x => ({
		nodeIds: [Guid.parse("59380f43-a464-4d3a-a863-e23746e6541c")],
		edgeIds: [Guid.parse("61c935f5-2178-4a14-ad21-4e69c2e60ad7")]
	}))


	graphs: GraphItem[];

	constructor(
		private graphService: GraphService
	) {
		super();
	}

	ngOnInit(): void {

		const lookup = GraphService.DefaultGraphListingLookup();

		this.graphService.query(lookup)
		.pipe(
			takeUntil(this._destroyed)
		)
		.subscribe(response => {
			this.graphs = response?.items?.map(graph => ({
				title: graph.name,
				description: graph.description,
				edgeIds: graph.graphEdges?.map(x => x.edge?.id).filter(x => !!x),
				nodeIds: graph.graphNodes?.map(x => x.node?.id).filter(x => !!x),
			}));
		});
	}

}


interface GraphItem{
	title: string;
	description: string;
	nodeIds: Guid[];
	edgeIds: Guid[];
}