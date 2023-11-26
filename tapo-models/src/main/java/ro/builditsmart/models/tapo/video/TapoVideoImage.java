package ro.builditsmart.models.tapo.video;

import lombok.Data;

@Data
public class TapoVideoImage {

    private String uri;

    private long length;

    private long uriExpiresAt;

}
