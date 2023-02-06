import { EdgeData } from "./edge-data.model";
import { NodeData } from "./node-data.model";

export interface GraphData {
	edges: EdgeData[];
	nodes: NodeData[];
	size: number;
}
