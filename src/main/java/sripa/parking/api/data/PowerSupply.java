package sripa.parking.api.data;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum PowerSupply {
  GASOLINE, ELECTRIC_20KW, ELECTRIC_50KW;
  private static final Set<String> lookup = Arrays
      .stream(PowerSupply.values())
      .map(Enum::name)
      .collect(Collectors.toSet());

  public static boolean contains(String powerSupply) {
    return lookup.contains(powerSupply);
  }
}
