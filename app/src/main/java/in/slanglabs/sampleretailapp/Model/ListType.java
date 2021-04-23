package in.slanglabs.sampleretailapp.Model;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({ListType.GROCERY, ListType.PHARMACY, ListType.FASHION})
@Retention(RetentionPolicy.SOURCE)
public @interface ListType {
    String GROCERY = "grocery";
    String PHARMACY = "pharmacy";
    String FASHION = "fashion";
}
