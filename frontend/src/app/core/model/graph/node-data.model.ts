import { EdgeData } from "./edge-data.model";

export interface NodeData {
	id: string;
	name: string;
	x: number;
	y: number;
	edges: EdgeData[];
	properties: Record<string, any>;
}
