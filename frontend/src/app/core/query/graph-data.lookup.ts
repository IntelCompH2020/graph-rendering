import { IsActive } from '@app/core/enum/is-active.enum';
import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { DoubleCompare } from './double-compare';

export class GraphDataLookup extends Lookup implements GraphDataFilter {
	ids: string[];
	excludedIds: string[];
	like: string;
	edgeIds: Guid[];
	nodeIds: Guid[];
	x: DoubleCompare[];
	y: DoubleCompare[];

	constructor() {
		super();
	}
}

export interface GraphDataFilter {
	ids: string[];
	excludedIds: string[];
	like: string;
	edgeIds: Guid[];
	nodeIds: Guid[];
	x: DoubleCompare[];
	y: DoubleCompare[];
}
