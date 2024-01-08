package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.db.dataplatform.techtest.TestDataHelper.TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@Mock
	private Server serverMock;

	private DataEnvelope testDataEnvelope;
	private DataBodyEntity testDataBodyEntity;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;

	@Before
	public void setUp() throws HadoopClientException, NoSuchAlgorithmException, IOException {
		serverController = new ServerController(serverMock);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);
	}

	@Test
	public void testPushDataPostCallWorksAsExpected() throws Exception {

		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
						.content(testDataEnvelopeJson)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(checksumPass).isTrue();
	}

	@Test
	public void testGetDataCallNotContentWorksAsExpected() throws Exception {
		ResultActions resultActions =
				mockMvc
						.perform(get(URI_GETDATA.toString(), BlockTypeEnum.BLOCKTYPEA.name()))
						.andExpect(status().isOk())
						.andDo(print());
		MvcResult result = resultActions.andReturn();
		assertThat(
				JsonPath.read(result.getResponse().getContentAsString(), "$.statusCode")
						.equals(HttpStatus.NO_CONTENT.value()))
				.isTrue();
	}

	@Test
	public void testGetDataCallWorksAsExpected() throws Exception {
		// Setup
		Instant expectedTimestamp = Instant.now();
		DataHeaderEntity dataHeaderEntity =
				TestDataHelper.createTestDataHeaderEntity(expectedTimestamp);

		testDataBodyEntity = TestDataHelper.createTestDataBodyEntity(dataHeaderEntity);
		List<DataBodyEntity> listDataBody = Arrays.asList(testDataBodyEntity);
		when(serverMock.getAllBlockTypes(any(BlockTypeEnum.class))).thenReturn(listDataBody);

		// Action
		ResultActions resultActions =
				mockMvc
						.perform(get(URI_GETDATA.toString(), BlockTypeEnum.BLOCKTYPEA.name()))
						.andExpect(status().isOk())
						.andDo(print());

		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		List<DataEnvelope> response = objectMapper.readValue(contentAsString, List.class);

		// Expected
		assertThat(response.size() > 0).isTrue();
		assertThat(
				JsonPath.read(result.getResponse().getContentAsString(), "$[0].dataHeaderEntity.name")
						.equals("Test"))
				.isTrue();
	}

	@Test
	public void testPatchDataCallWorksAsExpected() throws Exception {
		// Setup
		Instant expectedTimestamp = Instant.now();
		DataHeaderEntity dataHeaderEntity =
				TestDataHelper.createTestDataHeaderEntity(expectedTimestamp);

		testDataBodyEntity = TestDataHelper.createTestDataBodyEntity(dataHeaderEntity);
		List<DataBodyEntity> listDataBody = Arrays.asList(testDataBodyEntity);
		when(serverMock.updateByBlockName(any(String.class), any(String.class))).thenReturn(true);

		// Action
		ResultActions resultActions =
				mockMvc
						.perform(
								patch(URI_PATCHDATA.toString(), TEST_NAME, BlockTypeEnum.BLOCKTYPEB.name()))
						.andExpect(status().isAccepted())
						.andDo(print());

		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		boolean response = objectMapper.readValue(contentAsString, Boolean.class);

		// Expected
		assertThat(response).isTrue();
	}
}
