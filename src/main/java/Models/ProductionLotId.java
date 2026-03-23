package Models;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductionLotId implements Serializable {

    private Long productionId;
    private Long lotId;

    public Long getProductionId() { return productionId; }
    public void setProductionId(Long productionId) { this.productionId = productionId; }

    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductionLotId that)) return false;
        return Objects.equals(productionId, that.productionId) &&
                Objects.equals(lotId, that.lotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productionId, lotId);
    }
}
