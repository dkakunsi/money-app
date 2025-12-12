package io.dkakunsi.lab;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import io.dkakunsi.lab.common.data.Query;
import io.dkakunsi.lab.common.data.Query.Criteria;
import io.dkakunsi.lab.common.data.Query.Projection;

public final class JSONQuery extends Query {

  public JSONQuery(List<Criteria> criteria, Projection projection) {
    super(criteria, projection);
  }

  public static JSONQuery create(String content) {
    var json = new JSONObject(content);
    var criteria = JSONCriteria.create(json);
    var projection = JSONProjection.create(json);
    return new JSONQuery(criteria, projection);
  }
}

final class JSONCriteria extends Criteria {

  private JSONCriteria(String attribute, Operator operator, String value) {
    super(attribute, operator, value);
  }

  public static List<Criteria> create(JSONObject json) {
    var criteriaArr = json.optJSONArray("criteria");
    if (criteriaArr == null || criteriaArr.isEmpty()) {
      throw new IllegalArgumentException("No criteria");
    }
    var criterias = new ArrayList<Criteria>();
    criteriaArr.forEach(c -> {
      var jo = (JSONObject) c;
      var operator = jo.getString("operator");
      var attribute = jo.getString("attribute");
      var value = jo.getString("value");
      var criteria = new JSONCriteria(attribute, Operator.valueOfIgnoreCase(operator), value);
      criterias.add(criteria.validate());
    });
    return criterias;
  }
}

final class JSONProjection extends Projection {

  public static Projection create(JSONObject json) {
    var pJson = json.optJSONObject("projection");
    return buildProjection(pJson);
  }

  private static Projection buildProjection(JSONObject pJson) {
    var projection = new JSONProjection();
    if (pJson == null) {
      return projection;
    }
    pJson.keySet().forEach(k -> {
      var v = pJson.get(k);
      if (v instanceof JSONObject) {
        projection.addElement(k, buildProjection((JSONObject) v));
      } else {
        projection.addElement(k, new JSONProjection());
      }
    });
    return projection;
  }
}
