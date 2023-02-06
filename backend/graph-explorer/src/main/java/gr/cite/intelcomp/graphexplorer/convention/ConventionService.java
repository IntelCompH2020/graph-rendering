package gr.cite.intelcomp.graphexplorer.convention;

import gr.cite.tools.exception.MyApplicationException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public interface ConventionService {
	Boolean isValidId(Integer id);

	Boolean isValidGuid(UUID guid);

	Boolean isValidUUID(String str);
	UUID parseUUIDSafe(String str);

	Boolean isValidHash(String hash);

	String hashValue(Object value) throws MyApplicationException;

	String limit(String text, int maxLength);

	String truncate(String text, int maxLength);

	UUID getEmptyUUID();

	boolean isNullOrEmpty(String value);

	boolean isListNullOrEmpty(List value);

	String stringEmpty();

	String asPrefix(String name);

	Set<String> withPrefix(String  prefix, Set<String> items);

	String asIndexerPrefix(String part);

	String asIndexer(String... names);

	<K, V> Map<K, List<V>> toDictionaryOfList(List<V> items, Function<V, K> keySelector);
}
