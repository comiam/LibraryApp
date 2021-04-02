package comiam.nsu.libapp.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Pair<T, V>
{
	@Getter@Setter
	private T first;
	@Getter@Setter
	private V second;
}
