import { IsActive } from "@app/core/enum/is-active.enum";
import { Guid } from "@common/types/guid";
import { Moment } from "moment";
import { User } from "../user/user.model";
import { FieldDefinition } from "./field-definition.model";

export interface Edge{
    id: Guid;
    code: string;
    name: string;
    description: string;
    createdAt: Moment;
    updatedAt: Moment;
    isActive: IsActive;
    hash: string;
    config: EdgeConfig;
    edgeAccesses: EdgeAccess[];
}

export interface EdgeConfig{
    fields: FieldDefinition;
}

export interface EdgeAccess{
    id: Guid;
    user: User;
    isActive: IsActive;
    hash: string;
    createdAt: Moment;
    updatedAt: Moment;
}