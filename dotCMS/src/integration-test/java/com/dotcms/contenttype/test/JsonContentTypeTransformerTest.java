package com.dotcms.contenttype.test;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dotcms.contenttype.business.ContentTypeFactory;
import com.dotcms.contenttype.business.ContentTypeFactoryImpl;
import com.dotcms.contenttype.exception.NotFoundInDbException;
import com.dotcms.contenttype.model.field.Field;
import com.dotcms.contenttype.model.field.FieldVariable;
import com.dotcms.contenttype.model.type.ContentType;
import com.dotcms.contenttype.transform.contenttype.JsonContentTypeTransformer;
import com.dotcms.contenttype.transform.field.JsonFieldTransformer;
import com.dotcms.repackage.org.apache.tika.io.IOUtils;
import com.dotmarketing.business.APILocator;

public class JsonContentTypeTransformerTest {

  final ContentTypeFactory factory = new ContentTypeFactoryImpl();

  @BeforeClass
  public static void SetUpTests() throws FileNotFoundException, Exception {
    SuperContentTypeTest.SetUpTests();
  }

  @Test
  public void testContentTypeSerialization() throws Exception {
    List<ContentType> types = factory.findAll();
    for (ContentType type : types) {
      ContentType type2 = null;
      String json = null;
      try {
        json = new JsonContentTypeTransformer(type).json();
        type2 = new JsonContentTypeTransformer(json).from();

        assertThat("ContentType == ContentType2", type.equals(type2));
      } catch (Throwable t) {
        System.out.println(type);
        System.out.println(type2);
        System.out.println(json);
        throw t;
      }
    }
  }

  @Test
  public void testContentTypeArraySerialization() throws Exception {
    List<ContentType> types = factory.findAll();

    List<ContentType> types2 = null;
    String json = null;
    try {
      json = new JsonContentTypeTransformer(types).json();
      types2 = new JsonContentTypeTransformer(json).asList();

      for (int i = 0; i < types.size(); i++) {
        assertThat("ContentType == ContentType2", types.get(i).equals(types2.get(i)));
      }



    } catch (Throwable t) {
      System.out.println(types);
      System.out.println(types2);
      System.out.println(json);
      throw t;
    }
  }

  @Test
  public void testFieldSerialization() throws Exception {
    List<ContentType> types = factory.findAll();
    for (ContentType type : types) {
      Field field2 = null;
      String json = null;
      for (Field field : type.fields()) {

        try {
          json = new JsonFieldTransformer(field).json();

          field2 = new JsonFieldTransformer(json).from();


          assertThat("Field1 == json Field2", field.equals(field2));
        } catch (Throwable t) {
          System.out.println(json);
          System.out.println(field);
          System.out.println(field2);
          System.out.println(t);
          throw t;
        }
      }
    }
  }

  @Test
  public void testFieldArraySerialization() throws Exception {
    List<ContentType> types = factory.findAll();

    List<Field> fields = null;
    List<Field> fields2 = null;
    String json = null;
    try {
      for (ContentType type : types) {
        fields = type.fields();


        json = new JsonFieldTransformer(type.fields()).json();
        fields2 = new JsonFieldTransformer(json).asList();

        for (int i = 0; i < type.fields().size(); i++) {
          assertThat("Field1 == Field2", fields.get(i).equals(type.fields().get(i)));
        }
        for (int i = 0; i < type.fields().size(); i++) {
          assertThat("Field1 == Field2", fields2.get(i).equals(type.fields().get(i)));
        }


      }



    } catch (Throwable t) {
      System.out.println(fields);
      System.out.println(json);
      throw t;
    }
  }

  @Test
  public void testJsonToContentTypeArray() throws Exception {

    try{
      ContentType type = APILocator.getContentTypeAPI2(APILocator.systemUser()).find("banner");
      APILocator.getContentTypeAPI2(APILocator.systemUser()).delete(type);
    }
    catch(NotFoundInDbException e){
      
    }
    

    InputStream stream = this.getClass().getResourceAsStream("/com/dotcms/contenttype/test/content-type-array.json");
    String json = IOUtils.toString(stream);
    stream.close();
    ContentType type = new JsonContentTypeTransformer(json).from();
    List<Field> fields = new JsonFieldTransformer(json).asList();
    List<FieldVariable> vars = fields.get(0).fieldVariables();



    APILocator.getContentTypeAPI2(APILocator.systemUser()).save(type);

  }

  @Test
  public void testJsonToContentTypeObject() throws Exception {

    try{
      ContentType type = APILocator.getContentTypeAPI2(APILocator.systemUser()).find("banner");
      APILocator.getContentTypeAPI2(APILocator.systemUser()).delete(type);
    }
    catch(NotFoundInDbException e){
      
    }
    
    InputStream stream = this.getClass().getResourceAsStream("/com/dotcms/contenttype/test/content-type-object.json");
    String json = IOUtils.toString(stream);
    stream.close();
    ContentType type = new JsonContentTypeTransformer(json).from();
    List<Field> fields = new JsonFieldTransformer(json).asList();
    List<FieldVariable> vars = fields.get(0).fieldVariables();



    APILocator.getContentTypeAPI2(APILocator.systemUser()).save(type);

  }


}
