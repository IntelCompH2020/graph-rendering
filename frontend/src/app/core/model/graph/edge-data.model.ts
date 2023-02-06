export interface EdgeData {
	sourceId: string;
	targetId: string;
	weight: number;
	properties: Record<string, any>;
}
