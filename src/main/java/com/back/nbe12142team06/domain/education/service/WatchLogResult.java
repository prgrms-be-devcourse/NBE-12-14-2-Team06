package com.back.nbe12142team06.domain.education.service;

public record WatchLogResult(
        double maxWatchedSec,
        boolean completed,
        boolean verified
) {
}