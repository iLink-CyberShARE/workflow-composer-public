package com.utep.ilink.swim.util;

import com.google.gson.*;
import com.utep.ilink.swim.models.Data;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;

public class DataAdapter extends TypeAdapter<Data> {

  //The following method does not get used, however, it is required by the API
  public Data read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    // String xy = reader.nextString();
    // String[] parts = xy.split(",");
    // int x = Integer.parseInt(parts[0]);
    // int y = Integer.parseInt(parts[1]);
    // return new Point(x, y);
    return new Data("0",0);
  }

  //How Gson should convert a data object to json
  public void write(JsonWriter writer, Data data) throws IOException {
    if (data == null) {
      writer.nullValue();
      return;
    }
    writer.value(data.getId());
  }
}
