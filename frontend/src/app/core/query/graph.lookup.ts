import { Lookup } from '@common/model/lookup';
import { IsActive } from '../enum/is-active.enum';
export class GraphLookup extends Lookup implements GraphFilter {
	ids: string[];
	excludedIds: string[];
	like: string;
    isActive: IsActive;


	constructor() {
		super();
	}
}

export interface GraphFilter {
	ids: string[];
	excludedIds: string[];
	like: string;
    isActive: IsActive;
}
