package jdart.compiler.flow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdart.compiler.type.Type;
import jdart.compiler.type.Types;

public class Profiles {
  final LinkedHashMap<List<Type>, ProfileInfo> profileMap = new LinkedHashMap<>();

  public Map<List<Type>, ProfileInfo> getSignatureMap() {
    return profileMap;
  }
  
  @Override
  public String toString() {
    return profileMap.toString();
  }
  
  public Type lookupForACompatibleSignature(List<Type> argumentTypes) {
    ProfileInfo profileInfo = profileMap.get(argumentTypes);
    if (profileInfo != null) {
      return profileInfo.getReturnType();
    }
    
    for(Entry<List<Type>, ProfileInfo> entry: profileMap.entrySet()) {
     if (Types.isCompatible(entry.getKey(), argumentTypes)) {
        return entry.getValue().getReturnType();
      }
    }
    return null;
  }
}