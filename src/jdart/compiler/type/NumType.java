package jdart.compiler.type;

public interface NumType extends Type {
  Type add(Type other);

  Type sub(Type other);

  Type mod(Type other);
}
