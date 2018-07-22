package io.apicollab.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CollectionWrapperDTO<T> {
    private Integer totalCount;
    private Collection<T> items;

    public CollectionWrapperDTO(Collection<T> items) {
        this.items = items;
        if(items != null){
            this.totalCount = items.size();
        }
    }

}
