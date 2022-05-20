package io.nosqlbench.activitytype.cql.datamappers.functions.rainbow;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/**
 * Utility function used for advanced data generation experiments.
 */
@ThreadSafeMapper
@Categories({Category.experimental})
public class TokenMapFileToken extends TokenMapFileBaseFunction {

    public TokenMapFileToken(String filename, boolean loopdata, boolean ascending) {
        super(filename, loopdata, false, ascending);
    }

    @Override
    public long applyAsLong(int value) {
        TokenMapFileAPIService datasvc = tl_DataSvc.get();
        return datasvc.getToken();
    }
}