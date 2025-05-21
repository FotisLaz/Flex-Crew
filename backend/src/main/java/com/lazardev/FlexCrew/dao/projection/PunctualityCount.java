package com.lazardev.FlexCrew.dao.projection;

/**
 * Projection for retrieving punctuality counts grouped by status name.
 */
public interface PunctualityCount {
    String getStatusName();

    Long getCount();
}