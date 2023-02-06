import { Component, Input, OnInit } from '@angular/core';
import { GraphInfoLookup } from '@app/core/model/graph/graph-info.lookup';
import { Guid } from '@common/types/guid';

@Component({
  selector: 'app-graph-list-item-card',
  templateUrl: './graph-list-item-card.component.html',
  styleUrls: ['./graph-list-item-card.component.scss']
})
export class GraphListComponent implements OnInit {


  @Input()
  title: string  = '';

  @Input()
  description: string = '';

  @Input()
  nodeIds: Guid[];

  @Input()
	edgeIds: Guid[];

  constructor() { }

  ngOnInit(): void {
  }

}
