package org.opencb.datastore.mongodb;

import com.google.common.collect.Lists;
import com.mongodb.*;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencb.datastore.core.ComplexTypeConverter;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

/**
 * @author Ignacio Medina <imedina@ebi.ac.uk>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class MongoDBCollection {

    private DBCollection dbCollection;

    private MongoDBNativeQuery mongoDBNativeQuery;
    private long start;
    private long end;

    MongoDBCollection(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
        mongoDBNativeQuery = new MongoDBNativeQuery(dbCollection);
    }


    private void startQuery() {
        start = System.currentTimeMillis();
    }
    
    private QueryResult endQuery(List result, Class resultType, ComplexTypeConverter converter) {
        int numResults = (result != null) ? result.size() : 0;
        return endQuery(result, resultType, converter, numResults);
    }
    
    private QueryResult endQuery(List result, Class resultType, ComplexTypeConverter converter, int numTotalResults) {
        end = System.currentTimeMillis();
        int numResults = (result != null) ? result.size() : 0;
        
        QueryResult queryResult = new QueryResult(null, (int) (end-start), numResults, numTotalResults, null, null, result);
        // If a converter is provided, convert DBObjects to the requested type
        if (converter != null) {
            List convertedResult = new ArrayList<>(numResults);
            for (Object o : result) {
                convertedResult.add(converter.convertToDataModelType(o));
            }
            queryResult.setResult(convertedResult);
        } else {
            queryResult.setResult(result);
        }
        
//        queryResult.setNumResults((result != null) ? result.size() : 0);
//        queryResult.setNumTotalResults(numTotalResults);
//        queryResult.setResultType(resultType);
//        queryResult.setDBTime((int)(end-start));

        return queryResult;
        
    }
    
    public QueryResult count() {
        startQuery();
        long l = mongoDBNativeQuery.count();
        System.out.println(dbCollection.getStats());
        return endQuery(Arrays.asList(l), Long.class, null);
    }

    public QueryResult count(DBObject query) {
        startQuery();
        long l = mongoDBNativeQuery.count(query);
        return endQuery(Arrays.asList(l), Long.class, null);
    }

    public QueryResult distinct(String key, ComplexTypeConverter converter) {
        return distinct(key, null, converter);
    }

    public QueryResult distinct(String key, DBObject query, ComplexTypeConverter converter) {
        startQuery();
        List l = mongoDBNativeQuery.distinct(key, query);
        try {
            return endQuery(l,
                    converter == null ? DBObject.class :
                            Class.forName((((ParameterizedType) converter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0])
                                    .getClass().getCanonicalName()),
                    converter);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MongoDBCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public QueryResult find(DBObject query, QueryOptions options, ComplexTypeConverter converter) {
        return find(query, options, converter, null);
    }

    public QueryResult find(DBObject query, QueryOptions options, ComplexTypeConverter converter, DBObject returnFields) {
        startQuery();
        QueryResult queryResult = null;
        DBCursor cursor = mongoDBNativeQuery.find(query, returnFields, options);
        BasicDBList list = new BasicDBList();
        
        try {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    list.add(cursor.next());
                }
         
                if (options != null && options.getInt("limit") > 0) {
                    queryResult = endQuery(list, 
                            converter == null ? DBObject.class : 
                                    Class.forName((((ParameterizedType) converter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0])
                                            .getClass().getCanonicalName()), 
                            converter, cursor.count());
                } else {
                    queryResult = endQuery(list, 
                            converter == null ? DBObject.class : 
                                    Class.forName((((ParameterizedType) converter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0])
                                            .getClass().getCanonicalName()), 
                            converter);
                }
                
            } else {
                queryResult = endQuery(list, 
                            converter == null ? DBObject.class : 
                                    Class.forName((((ParameterizedType) converter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0])
                                            .getClass().getCanonicalName()), 
                            converter);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MongoDBCollection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return queryResult;
    }

    public QueryResult aggregate(Object id, List<DBObject> operations, QueryOptions options) {
        startQuery();
        QueryResult queryResult = new QueryResult();
        AggregationOutput output = mongoDBNativeQuery.aggregate(id, operations, options);
        queryResult.setResult(Lists.newArrayList(output.results()));
        queryResult.setNumTotalResults(queryResult.getNumResults());
        return queryResult;
    }

    public QueryResult insert(DBObject... object) {
        startQuery();
        WriteResult wr = mongoDBNativeQuery.insert(object);
        QueryResult queryResult = endQuery(Arrays.asList(wr), WriteResult.class, null);
        if (!wr.getLastError().ok()) {
            queryResult.setErrorMsg(wr.getLastError().getErrorMessage());
        }
        return queryResult;
    }

    public QueryResult update(DBObject object, DBObject updates, boolean upsert, boolean multi) {
        startQuery();
        WriteResult wr = mongoDBNativeQuery.update(object, updates, upsert, multi);
        QueryResult queryResult = endQuery(Arrays.asList(wr), WriteResult.class, null);
        if (!wr.getLastError().ok()) {
            queryResult.setErrorMsg(wr.getLastError().getErrorMessage());
        }
        return queryResult;
    }


    /**
     * Create a new Native instance.  This is a convenience method, equivalent to {@code new MongoClientOptions.Native()}.
     *
     * @return a new instance of a Native
     */
    public MongoDBNativeQuery nativeQuery() {
        return mongoDBNativeQuery;
    }

}
