import { IsActive } from "@app/core/enum/is-active.enum";
import { Guid } from "@common/types/guid";
import { Moment } from "moment";
import { User } from "../user/user.model";
import { FieldDefinition } from "./field-definition.model";

// * Node
export interface Node {
    id: Guid;
    code: string;
    name: string;
    description: string;
    createdAt: Moment;
    updatedAt: Moment;
    isActive: IsActive;
    hash: string;
    config: NodeConfig;
    nodeAccesses: NodeAccess[];
}

export interface NodeConfig{
    fields: FieldDefinition[];
    clusterFields: string[];
    defaultOrderField: string;
}

export interface NodeAccess{
    id: Guid;
    user: User;
    node: Node;
    isActive: IsActive;
    hash: string;
    createdAt: Moment;
    updatedAt: Moment;
}
