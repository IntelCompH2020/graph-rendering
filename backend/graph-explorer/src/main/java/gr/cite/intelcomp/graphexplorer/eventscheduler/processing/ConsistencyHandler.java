package gr.cite.intelcomp.graphexplorer.eventscheduler.processing;

public interface ConsistencyHandler<T extends ConsistencyPredicates> {
	Boolean isConsistent(T consistencyPredicates);
}
