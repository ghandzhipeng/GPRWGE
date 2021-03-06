package ge.PSUtils;

import com.tencent.angel.PartitionKey;
import com.tencent.angel.ml.matrix.psf.update.base.PartitionUpdateParam;
import com.tencent.angel.ml.matrix.psf.update.base.UpdateParam;
import com.tencent.angel.psagent.PSAgentContext;

import java.util.ArrayList;
import java.util.List;

public class GEPushParam extends UpdateParam {

    int[] indices;
    float[] deltas;
    int numNodePerRow;
    int dimension;


    public GEPushParam(int matrixId, int[] indices, float[] deltas, int numNodePerRow, int dimension) {
        super(matrixId);
        this.indices = indices;
        this.deltas = deltas;
        this.numNodePerRow = numNodePerRow;
        this.dimension = dimension;
    }

    @Override
    public List<PartitionUpdateParam> split() {
        List<PartitionKey> pkeys = PSAgentContext.get().getMatrixMetaManager().getPartitions(matrixId);
        List<PartitionUpdateParam> params = new ArrayList<PartitionUpdateParam>();

        int start = 0, end = 0;
        for (PartitionKey pkey: pkeys) {
            int startRow = pkey.getStartRow();
            int endRow   = pkey.getEndRow();
            int startNode = startRow * numNodePerRow;
            int endNode  = endRow * numNodePerRow;

            if (start < indices.length && indices[start] >= startNode) {
                while (end < indices.length && indices[end] < endNode)
                    end++;

                if (end > start)
                    params.add(new GEPushPartitionParam(matrixId,
                            pkey,
                            indices,
                            deltas,
                            numNodePerRow,
                            start,
                            end - start,
                            dimension));
                start = end;
            }
        }

        return params;
    }
}
