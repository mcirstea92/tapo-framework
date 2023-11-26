package ro.builditsmart.models.tapo.video;

import lombok.Data;

@Data
public class TapoVideo {

    private String uri;

    private long duration;

    private String m3u8;

    private long startTimestamp;

    private long uriExpiresAt;

}
