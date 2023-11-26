package ro.builditsmart.models.tapo.video;

import lombok.Data;

import java.util.List;

@Data
public class TapoVideoPageItem {

    private String uuid;

    private List<TapoVideo> video;

    private List<TapoVideoImage> image;

    private long createdTime;

    private String eventLocalTime;

}
