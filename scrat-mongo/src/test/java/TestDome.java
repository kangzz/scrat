import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.scrat.framework.mongo.utils.MongoDBUtil;
import model.Person;
import org.bson.Document;

/**
 * Created by kangzz on 16/11/15.
 */
public class TestDome {
    /**
     * 测试入口
     *
     * @param args
     */
    public static void main(String[] args) {

        String dbName = "myTest";
        String collName = "kangzz";
        MongoCollection<Document> coll = MongoDBUtil.instance.getCollection(dbName, collName);

        // 插入多条
        for (int i = 1; i <= 1; i++) {
            Document doc = new Document();
            doc.put("_id", "hjhjhhjh");
            doc.put("name", "zhoulf");
            doc.put("school", "NEFU" + i);
            Document interests = new Document();
            interests.put("game", "game" + i);
            interests.put("ball", "ball" + i);
            doc.put("interests", interests);
            //coll.insertOne(doc);
        }
        System.out.println( MongoDBUtil.instance.getAllCollections(dbName));
        BasicDBObject basicDBObject = new BasicDBObject();
        MongoCursor res= MongoDBUtil.instance.find(coll,basicDBObject);

        if(res!=null){
            while (res.hasNext()){
                Document document = (Document) res.next();
                System.out.println(document.toJson());
                System.out.println("-------------"+document.get("_id").toString());

                //System.out.println(JSONObject.parseObject(document.toJson()).getJSONObject("_id").getString("$oid"));
                //String str= JSONObject.toJSONString(res.next());
                //System.out.println(str);

            }
        }

        System.out.println("-----------");

        String id = "582ac1a3093e5e0f77f301a7";
        Document doc = MongoDBUtil.instance.findById(coll, id);
        System.out.println(doc.toJson());

        Person person = MongoDBUtil.instance.findById(coll, id,Person.class);


        //System.out.println(JSONObject.toJSONString(person)+"--"+person.get_id().toString());
        // // 根据ID查询
        // String id = "556925f34711371df0ddfd4b";
        // Document doc = MongoDBUtil2.instance.findById(coll, id);
        // System.out.println(doc);

        // 查询多个
        // MongoCursor<Document> cursor1 = coll.find(Filters.eq("name", "zhoulf")).iterator();
        // while (cursor1.hasNext()) {
        // org.bson.Document _doc = (Document) cursor1.next();
        // System.out.println(_doc.toString());
        // }
        // cursor1.close();

        // 查询多个
        // MongoCursor<Person> cursor2 = coll.find(Person.class).iterator();

        // 删除数据库
        // MongoDBUtil2.instance.dropDB("testdb");

        // 删除表
        // MongoDBUtil2.instance.dropCollection(dbName, collName);

        // 修改数据
        // String id = "556949504711371c60601b5a";
        // Document newdoc = new Document();
        // newdoc.put("name", "时候");
        // MongoDBUtil.instance.updateById(coll, id, newdoc);

        // 统计表
        // System.out.println(MongoDBUtil.instance.getCount(coll));

        // 查询所有
        //Bson filter = Filters.eq("count", 0);
        //MongoDBUtil.instance.find(coll, filter);

    }
}
