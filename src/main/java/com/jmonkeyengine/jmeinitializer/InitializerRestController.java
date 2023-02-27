package com.jmonkeyengine.jmeinitializer;

import com.jmonkeyengine.jmeinitializer.dto.MostRecentVersionSearchResult;
import com.jmonkeyengine.jmeinitializer.libraries.LibraryService;
import com.jmonkeyengine.jmeinitializer.uisupport.UiLibraryDataDto;
import com.jmonkeyengine.jmeinitializer.versions.FreeFormVersionSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Represents endpoints used by the front end
 */
@RestController
@AllArgsConstructor
public class InitializerRestController {

    private static final String GAME_NAME_DOC_STRING = "The name of the game, will be sanitised to something like MyExcellentGame. Caller is not required to sanitise";
    private static final String GAME_DESCRIPTION_DOC_STRING = "The proposed package for the games source, e.g. com.example. Can be blank. Caller is not required to sanitise";
    private static final String REQUIRED_LIBRARIES_DOC_STRING = "A comma delimited list of the library keys for the libraries the user requests. E.g. `JME_DESKTOP,LEMUR,LOG4J2`";
    private static final String DEPLOYMENT_OPTIONS_DOC_STRING = "A comma delimited list of the deployment option keys the user requests. E.g. `WINDOWS,LINUX`";


    private final InitializerZipService initializerZipService;
    private final LibraryService libraryService;
    private final FreeFormVersionSearchService freeFormVersionSearchService;

    @Operation( summary = "The available library description json", description = "Returns a json packet that includes data on all the libraries that the initializer has to offer. \n\nIntended to be used by a UI application to display the options available to the user")
    @GetMapping("/jme-initializer/libraries")
    public UiLibraryDataDto getDataForUi(){
        return libraryService.getUiLibraryDataDto();
    }

    @Operation(summary = "Build Starter zip", description = "Given details about the game/application will return a zip file containing a starter project")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "A zip of a new gradle project",
                    content = { @Content(mediaType = "application/octet-stream") })
    })
    @ResponseBody
    @GetMapping("/jme-initializer/zip")
    public ResponseEntity<Resource> buildStarterZip(
            @Parameter(description=GAME_NAME_DOC_STRING, example = "MyGame") @RequestParam String gameName,
            @Parameter(description=GAME_DESCRIPTION_DOC_STRING, example = "com.example") @RequestParam String packageName,
            @Parameter(description=REQUIRED_LIBRARIES_DOC_STRING, example = "JME_DESKTOP,LEMUR,LOG4J2") @RequestParam String libraryList,
            @Parameter(description=DEPLOYMENT_OPTIONS_DOC_STRING, example = "WINDOWS,LINUX") @RequestParam String deploymentOptionsList)
            throws IOException {

        try(ByteArrayOutputStream byteArrayOutputStream = initializerZipService.produceZipInMemory( gameName, packageName, Arrays.asList(libraryList.split(",")), Arrays.asList(deploymentOptionsList.split(",")) )){

            ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename( Merger.sanitiseToJavaClass(gameName) + ".zip") //a java class name is a valid file name as well. Seems a reasonable name for the zip
                                    .build().toString())
                    .body(resource);
        }
    }

    @Operation( summary = "Build starter project's build.gradle files", description = "Given details about the game/application will return a map of the names of the build.gradle files (including paths if appropriate) to their contents.\n\n Is intended to give end users a preview of libraries/structure they have requested before they get the full zip")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "A map of gradle file names to their contents",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"build.gradle\":\"string\", \"desktop/build.gradle\":\"string\"}")) })
    })
    @ResponseBody
    @GetMapping("/jme-initializer/gradle-preview")
    public ResponseEntity<Map<String, String>> previewGradleFile(
            @Parameter(description=GAME_NAME_DOC_STRING, example = "MyGame") @RequestParam String gameName,
            @Parameter(description=GAME_DESCRIPTION_DOC_STRING, example = "com.example") @RequestParam String packageName,
            @Parameter(description=REQUIRED_LIBRARIES_DOC_STRING, example = "JME_DESKTOP,LEMUR,LOG4J2") @RequestParam String libraryList,
            @Parameter(description=DEPLOYMENT_OPTIONS_DOC_STRING, example = "WINDOWS,LINUX") @RequestParam String deploymentOptionsList) {
        Map<String, String> gradleFile = initializerZipService.produceGradleFilePreview(gameName, packageName, Arrays.asList(libraryList.split(",")), Arrays.asList(deploymentOptionsList.split(",")) );

        return ResponseEntity.ok().body(gradleFile);
    }

    @Operation( summary = "Uses Maven Central to determine the most recent version of a library", description = "Uses Maven Central to determine the most recent version of a library, will cache internally for a period so will not hit maven central excessively. Note it uses the version number to determine most recent, not the publishing date. So 4.0.0 is more recent than emergency bugfix release 3.9.9 even if 3.9.9 was released after 4.0.0")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "The most recent (acceptable) version of the library",
                    content = { @Content(mediaType = "application/json", schema = @Schema(example = "{ \"found\":\"true\", \"version\":\"1.0.2\"}")) }),
    })
    @ResponseBody
    @GetMapping("/jme-initializer/most-recent-version-search")
    public ResponseEntity<MostRecentVersionSearchResult> getMostRecentVersion(
            @Parameter(description="Library group id", example = "com.onemillionworlds") @RequestParam String groupId,
            @Parameter(description="Library Artifact Id", example = "tamarin") @RequestParam String artifactId,
            @Parameter(description="Acceptable version regex (used to filter out beta releases etc)", example = "[\\.\\d]*") @RequestParam(defaultValue="[\\.\\d]*") String versionRegex) {

        Optional<String> version = freeFormVersionSearchService.fetchOrGetMostRecentVersion(groupId, artifactId, versionRegex);

        MostRecentVersionSearchResult searchResult;
        if (version.isPresent()){
            searchResult = new MostRecentVersionSearchResult(true, version.get());
        }else{
            searchResult = new MostRecentVersionSearchResult(false, "");
        }

        return ResponseEntity.ok(searchResult);
    }
 }
