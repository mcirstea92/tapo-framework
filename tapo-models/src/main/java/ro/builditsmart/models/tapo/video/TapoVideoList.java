package ro.builditsmart.models.tapo.video;

import lombok.Data;

import java.util.List;

@Data
public class TapoVideoList {

    private String deviceId;

    private int total;

    private int page;

    private int pageSize;

    private List<TapoVideoPageItem> index;

}
