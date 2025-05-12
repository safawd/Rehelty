package tn.esprit.interfaces;

import tn.esprit.models.Personne;

import java.util.List;

public interface IServices<T> {
    void add(T t);
    List<T> getAll();
    void update(T t);
    void delete(T t);

}