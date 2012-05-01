package models;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.query.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.bson.types.CodeWScope;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mathias Bogaert
 */
public abstract class Model {
    @Inject
    public static Datastore datastore; // requestStaticInjection(..)

    @Id
    @JsonIgnore
    public ObjectId id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (id != null ? !id.equals(model.id) : model.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Key key() {
        return datastore.getKey(this);
    }

    public void save() {
        datastore.save(this);
    }

    public void merge() {
        datastore.merge(this);
    }

    public WriteResult delete() {
        return datastore.delete(this);
    }

    /**
     * @author Mathias Bogaert
     */
    public static final class Finder<T extends Model> implements Query<T> {
        private final Class<T> type;

        public Finder(Class<T> type) {
            this.type = type;
        }

        public T byId(String id) {
            return datastore.get(type, ObjectId.massageToObjectId(id));
        }

        public T byId(ObjectId objectId) {
            return datastore.get(type, objectId);
        }

        public List<T> byIds(Iterable<ObjectId> ids) {
            if (ids == null || !ids.iterator().hasNext()) return Collections.emptyList();
            return datastore.get(type, ids).asList();
        }

        public long count() {
            return datastore.getCount(type);
        }

        public Query<T> query() {
            return datastore.find(type);
        }

        @Override
        public Query<T> filter(String condition, Object value) {
            return query().filter(condition, value);
        }

        @Override
        public FieldEnd<? extends Query<T>> field(String field) {
            return query().field(field);
        }

        @Override
        public FieldEnd<? extends CriteriaContainerImpl> criteria(String field) {
            return query().criteria(field);
        }

        @Override
        public CriteriaContainer and(Criteria... criteria) {
            return query().and(criteria);
        }

        @Override
        public CriteriaContainer or(Criteria... criteria) {
            return query().or(criteria);
        }

        @Override
        public Query<T> where(String js) {
            return query().where(js);
        }

        @Override
        public Query<T> where(CodeWScope js) {
            return query().where(js);
        }

        @Override
        public Query<T> order(String condition) {
            return query().order(condition);
        }

        @Override
        public Query<T> limit(int value) {
            return query().limit(value);
        }

        @Override
        public Query<T> batchSize(int value) {
            return query().batchSize(value);
        }

        @Override
        public Query<T> offset(int value) {
            return query().offset(value);
        }

        @Override
        @Deprecated
        public Query<T> skip(int value) {
            return query().skip(value);
        }

        @Override
        public Query<T> enableValidation() {
            return query().enableValidation();
        }

        @Override
        public Query<T> disableValidation() {
            return query().disableValidation();
        }

        @Override
        public Query<T> hintIndex(String idxName) {
            return query().hintIndex(idxName);
        }

        @Override
        public Query<T> retrievedFields(boolean include, String... fields) {
            return query().retrievedFields(include, fields);
        }

        @Override
        public Query<T> enableSnapshotMode() {
            return query().enableSnapshotMode();
        }

        @Override
        public Query<T> disableSnapshotMode() {
            return query().disableSnapshotMode();
        }

        @Override
        public Query<T> queryNonPrimary() {
            return query().queryNonPrimary();
        }

        @Override
        public Query<T> queryPrimaryOnly() {
            return query().queryPrimaryOnly();
        }

        @Override
        public Query<T> useReadPreference(ReadPreference readPref) {
            return query().useReadPreference(readPref);
        }

        @Override
        public Query<T> disableCursorTimeout() {
            return query().disableCursorTimeout();
        }

        @Override
        public Query<T> enableCursorTimeout() {
            return query().enableCursorTimeout();
        }

        @Override
        public Class<T> getEntityClass() {
            return type;
        }

        @Override
        public Query<T> clone() {
            return query().clone();
        }

        @Override
        public T get() {
            return query().get();
        }

        @Override
        public Key<T> getKey() {
            return query().getKey();
        }

        @Override
        public List<T> asList() {
            return query().asList();
        }

        @Override
        public List<Key<T>> asKeyList() {
            return query().asKeyList();
        }

        @Override
        public Iterable<T> fetch() {
            return query().fetch();
        }

        @Override
        public Iterable<T> fetchEmptyEntities() {
            return query().fetchEmptyEntities();
        }

        @Override
        public Iterable<Key<T>> fetchKeys() {
            return query().fetchKeys();
        }

        @Override
        public long countAll() {
            return query().countAll();
        }

        @Override
        public Iterator<T> tail() {
            return query().tail();
        }

        @Override
        public Iterator<T> tail(boolean awaitData) {
            return query().tail(awaitData);
        }

        @Override
        public Iterator<T> iterator() {
            return query().iterator();
        }
    }
}