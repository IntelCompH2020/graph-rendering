package gr.cite.intelcomp.graphexplorer.audit;

import gr.cite.tools.logging.EventId;

public class AuditableAction {
	public static final EventId IdentityTracking_Action = new EventId(1000, "IdentityTracking_Action");
	public static final EventId IdentityTracking_User_Persist = new EventId(1001, "IdentityTracking_User_Persist");
	public static final EventId IdentityTracking_ForgetMe_Request = new EventId(1002, "IdentityTracking_ForgetMe_Request");
	public static final EventId IdentityTracking_ForgetMe_Validate = new EventId(1003, "IdentityTracking_ForgetMe_Validate");
	public static final EventId IdentityTracking_ForgetMe_Stamp = new EventId(1004, "IdentityTracking_ForgetMe_Stamp");
	public static final EventId Principal_Lookup = new EventId(2000, "Principal_Lookup");

	public static final EventId User_Query = new EventId(3000, "User_Query");
	public static final EventId User_Lookup = new EventId(3001, "User_Lookup");
	public static final EventId User_Persist = new EventId(3002, "User_Persist");
	public static final EventId User_Delete = new EventId(3003, "User_Delete");
	public static final EventId User_PersistAccess = new EventId(3004, "User_PersistAccess");
	
	public static final EventId Scheduled_Event_Persist = new EventId(4000, "Scheduled_Event_Persist");
	public static final EventId Scheduled_Event_Canceled = new EventId(4001, "Scheduled_Event_Canceled");
	public static final EventId Scheduled_Event_Run = new EventId(4002, "Scheduled_Event_Run");

	public static final EventId Node_Query = new EventId(5000, "Node_Query");
	public static final EventId Node_Lookup = new EventId(5001, "Node_Lookup");
	public static final EventId Node_Persist = new EventId(5002, "Node_Persist");
	public static final EventId Node_Delete = new EventId(5003, "Node_Delete");
	public static final EventId Node_Data_Persist = new EventId(5004, "Node_Data_Persist");
	public static final EventId Node_Data_Bulk_Persist = new EventId(5005, "Node_Data_Bulk_Persist");
	public static final EventId Node_Data_Query = new EventId(5006, "Node_Data_Query");
	public static final EventId Node_Data_Lookup = new EventId(5007, "Node_Data_Lookup");

	public static final EventId Edge_Query = new EventId(6000, "Edge_Query");
	public static final EventId Edge_Lookup = new EventId(6001, "Edge_Lookup");
	public static final EventId Edge_Persist = new EventId(6002, "Edge_Persist");
	public static final EventId Edge_Delete = new EventId(6003, "User_Delete");
	public static final EventId Edge_Data_Persist = new EventId(6004, "Edge_Data_Persist");
	public static final EventId Edge_Data_Bulk_Persist = new EventId(6005, "Edge_Data_Persist");
	public static final EventId Edge_Data_Query = new EventId(6006, "Edge_Data_Query");

	public static final EventId Graph_Query = new EventId(7000, "Graph_Query");
	public static final EventId Graph_Lookup = new EventId(7001, "Edge_Lookup");
	public static final EventId Graph_Persist = new EventId(7002, "Edge_Persist");
	public static final EventId Graph_Delete = new EventId(7003, "User_Delete");
	public static final EventId Graph_QueryData = new EventId(7004, "Graph_QueryData");
	public static final EventId Graph_GraphInfo = new EventId(7005, "Graph_GraphInfo");
	public static final EventId Graph_RecalculateNodeSize = new EventId(7006, "Graph_RecalculateNodeSize");
}
