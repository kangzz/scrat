package model;

import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Created by kangzz on 16/11/15.
 */
public class Person implements Serializable{
    private static final long serialVersionUID = -7259393949877964377L;
    private ObjectId _id;
    private String name;
    private String school;
    private Interests interests;
    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Interests getInterests() {
        return interests;
    }

    public void setInterests(Interests interests) {
        this.interests = interests;
    }
}
