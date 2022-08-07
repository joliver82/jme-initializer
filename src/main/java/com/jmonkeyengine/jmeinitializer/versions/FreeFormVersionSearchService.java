package com.jmonkeyengine.jmeinitializer.versions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * This service supports free form access to the logic that pulls the most recent version of a library
 * from maven central
 */
@Service
public class FreeFormVersionSearchService{

    /**
     * Key is group:artifactId:regex value is the most recent version
     */
    LoadingCache<SearchParameters, Optional<String>> versionsCache;

    VersionService versionService;

    public FreeFormVersionSearchService(VersionService versionService){
        versionsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<>(){
                    @Override
                    public Optional<String> load(SearchParameters key){
                        return versionService.fetchMostRecentStableVersion(key.groupId, key.artifactId, key.acceptableVersionRegex);
                    }
                });

    }

    public Optional<String> fetchOrGetMostRecentVersion(String groupId, String artifactId, String acceptableVersionRegex){
        try{
            return versionsCache.get(new SearchParameters(groupId, artifactId, acceptableVersionRegex));
        } catch(ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    private record SearchParameters(String groupId, String artifactId, String acceptableVersionRegex ){}
}
