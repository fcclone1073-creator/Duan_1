package ph61167.dunghn.duan.data.remote;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ErrorResponseConverter implements Converter<ResponseBody, String> {
    private static final String TAG = "ErrorResponseConverter";
    private final Gson gson;

    public ErrorResponseConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String convert(ResponseBody value) throws IOException {
        try {
            String jsonString = value.string();
            Log.d(TAG, "Error response body: " + jsonString);
            
            // Thử parse JSON để lấy message
            try {
                JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                if (jsonObject.has("message")) {
                    return jsonObject.get("message").getAsString();
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not parse error JSON: " + e.getMessage());
            }
            
            return jsonString;
        } finally {
            value.close();
        }
    }

    public static class Factory extends Converter.Factory {
        private final Gson gson;

        public Factory(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(
                Type type,
                Annotation[] annotations,
                Retrofit retrofit) {
            if (type == String.class) {
                return new ErrorResponseConverter(gson);
            }
            return null;
        }
    }
}

