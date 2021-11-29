package com.zhouzifei.simplefile.common.serializer;



import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.zhouzifei.simplefile.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class Date2StringSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (Objects.isNull(date)) {
            jsonGenerator.writeString(StringUtils.EMPTY);
        } else {
            jsonGenerator.writeString(DateUtil.stdDate2String(date));
        }
    }
}
