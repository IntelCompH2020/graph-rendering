import { IsActive } from "@app/core/enum/is-active.enum";
import { Guid } from "@common/types/guid";
import { Moment } from "moment";
import { User } from "../user/user.model";
import { Edge } from "./edge.model";
import { Node } from './node.model';
export interface Graph{
    id: Guid;
    name: string;
    description: string;
    createdAt: Moment;
    updatedAt: Moment;
    isActive: IsActive;
    hash: string;
    graphNodes: GraphNode[];
    graphEdges: GraphEdge[];
    graphAccesses: GraphAccess[];
}

export interface GraphNode{
    id: Guid;
    node: Node;
    graph: Graph;
    isActive: IsActive;
    hash: string;
    createdAt: Moment;
    updatedAt: Moment;
}
export interface GraphEdge{
    id: Guid;
    edge: Edge;
    graph: Graph;
    isActive: IsActive;
    hash: string;
    createdAt: Moment;
    updatedAt: Moment;
}
export interface GraphAccess{
    id: Guid;
    user: User;
    graph: Graph;
    isActive: IsActive;
    hash: string;
    createdAt: Moment;
    updatedAt: Moment;
}

