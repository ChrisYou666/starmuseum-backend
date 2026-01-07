package com.starmuseum.starmuseum.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starmuseum.starmuseum.location.entity.Location;
import com.starmuseum.starmuseum.location.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;




import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web 层测试：只验证 LocationController 的路由、参数绑定、返回结构、异常分支
 * - 不连 DB
 * - LocationService 使用 Mock
 *
 * 运行：
 *   mvn -Dtest=LocationControllerTest test
 */
@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @MockitoBean
    private LocationService locationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========= 工具：兼容“异常时可能是 HTTP 404，也可能是 HTTP 200 + body.code=404”两种风格 =========
    private void assertBizError(MvcResult result, int expectedCode, String expectedMsgContains) throws Exception {
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        // 1) 如果你们全局异常处理走“HTTP 状态码”
        if (status == expectedCode) {
            if (expectedMsgContains != null && !expectedMsgContains.isBlank()) {
                assertThat(body).contains(expectedMsgContains);
            }
            return;
        }

        // 2) 如果你们全局异常处理走“HTTP 200 + Result.code”
        if (status == 200) {
            assertThat(body).contains("\"code\":" + expectedCode);
            if (expectedMsgContains != null && !expectedMsgContains.isBlank()) {
                assertThat(body).contains(expectedMsgContains);
            }
            return;
        }

        // 3) 都不是就判失败，便于你快速定位
        throw new AssertionError("期望业务错误 code=" + expectedCode + "，但实际 HTTP status=" + status + "，body=" + body);
    }

    @Test
    @DisplayName("GET /api/locations - list 正常返回")
    void list_ok() throws Exception {
        Location l1 = new Location();
        l1.setId(1L);
        l1.setName("Jakarta");
        l1.setCountry("Indonesia");
        l1.setCity("Jakarta");
        l1.setLatitude(-6.2);
        l1.setLongitude(106.8);
        l1.setTimezone("Asia/Jakarta");

        when(locationService.list()).thenReturn(List.of(l1));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                // 尽量不强绑定 Result 结构，只校验 data 存在且内容正确
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Jakarta"));

        verify(locationService, times(1)).list();
        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("GET /api/locations/{id} - 存在时返回")
    void get_found() throws Exception {
        Location l = new Location();
        l.setId(10L);
        l.setName("Beijing");
        l.setCountry("China");
        l.setCity("Beijing");
        l.setLatitude(39.9);
        l.setLongitude(116.4);
        l.setTimezone("Asia/Shanghai");

        when(locationService.getById(10L)).thenReturn(l);

        mockMvc.perform(get("/api/locations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Beijing"));

        verify(locationService, times(1)).getById(10L);
        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("GET /api/locations/{id} - 不存在时抛 BusinessException(404)")
    void get_notFound() throws Exception {
        when(locationService.getById(999L)).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/api/locations/999"))
                .andReturn();

        assertBizError(result, 404, "Location 不存在");

        verify(locationService, times(1)).getById(999L);
        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("POST /api/locations - create 成功：会调用 save 且字段映射正确")
    void create_ok() throws Exception {
        when(locationService.save(any(Location.class))).thenReturn(true);

        String body = """
                {
                  "name": "Bandung",
                  "country": "Indonesia",
                  "city": "Bandung",
                  "latitude": -6.9147,
                  "longitude": 107.6098,
                  "timezone": "Asia/Jakarta"
                }
                """;

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bandung"))
                .andExpect(jsonPath("$.data.country").value("Indonesia"))
                .andExpect(jsonPath("$.data.city").value("Bandung"));

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationService, times(1)).save(captor.capture());
        Location saved = captor.getValue();

        assertThat(saved.getId()).isNull(); // create 里没设置 id（通常由 DB 生成）
        assertThat(saved.getName()).isEqualTo("Bandung");
        assertThat(saved.getCountry()).isEqualTo("Indonesia");
        assertThat(saved.getCity()).isEqualTo("Bandung");
        assertThat(saved.getLatitude()).isEqualTo(-6.9147);
        assertThat(saved.getLongitude()).isEqualTo(107.6098);
        assertThat(saved.getTimezone()).isEqualTo("Asia/Jakarta");

        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("POST /api/locations - create 参数校验失败：@Valid 触发（若 DTO 有 NotBlank/NotNull）")
    void create_validationFail() throws Exception {
        // 空 body 或缺字段，让 @Valid 尝试触发校验
        String body = "{}";

        MvcResult result = mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        int status = result.getResponse().getStatus();
        String respBody = result.getResponse().getContentAsString();

        // 校验失败通常是 400；如果你们包装成 200 + code=400，也兼容
        if (status == 400) {
            // ok
        } else if (status == 200) {
            assertThat(respBody).contains("\"code\":400");
        } else {
            throw new AssertionError("期望校验失败返回 400 或 200+code=400，但实际 status=" + status + "，body=" + respBody);
        }

        verifyNoInteractions(locationService);
    }

    @Test
    @DisplayName("PUT /api/locations/{id} - update 成功：先 getById，再 updateById，字段映射正确")
    void update_ok() throws Exception {
        Location found = new Location();
        found.setId(5L);
        found.setName("OldName");
        found.setCountry("OldCountry");
        found.setCity("OldCity");
        found.setLatitude(0.0);
        found.setLongitude(0.0);
        found.setTimezone("UTC");

        when(locationService.getById(5L)).thenReturn(found);
        when(locationService.updateById(any(Location.class))).thenReturn(true);

        String body = """
                {
                  "name": "Surabaya",
                  "country": "Indonesia",
                  "city": "Surabaya",
                  "latitude": -7.2575,
                  "longitude": 112.7521,
                  "timezone": "Asia/Jakarta"
                }
                """;

        mockMvc.perform(put("/api/locations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Surabaya"));

        // 验证调用顺序 & 入参内容
        verify(locationService, times(1)).getById(5L);

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationService, times(1)).updateById(captor.capture());
        Location updated = captor.getValue();

        assertThat(updated.getId()).isEqualTo(5L);
        assertThat(updated.getName()).isEqualTo("Surabaya");
        assertThat(updated.getCountry()).isEqualTo("Indonesia");
        assertThat(updated.getCity()).isEqualTo("Surabaya");
        assertThat(updated.getLatitude()).isEqualTo(-7.2575);
        assertThat(updated.getLongitude()).isEqualTo(112.7521);
        assertThat(updated.getTimezone()).isEqualTo("Asia/Jakarta");

        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("PUT /api/locations/{id} - update 不存在：BusinessException(404)")
    void update_notFound() throws Exception {
        when(locationService.getById(404L)).thenReturn(null);

        String body = """
                {
                  "name": "X",
                  "country": "Y",
                  "city": "Z",
                  "latitude": 1.0,
                  "longitude": 2.0,
                  "timezone": "UTC"
                }
                """;

        MvcResult result = mockMvc.perform(put("/api/locations/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        assertBizError(result, 404, "Location 不存在");

        verify(locationService, times(1)).getById(404L);
        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} - delete 成功")
    void delete_ok() throws Exception {
        when(locationService.removeById(7L)).thenReturn(true);

        mockMvc.perform(delete("/api/locations/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("deleted"));

        verify(locationService, times(1)).removeById(7L);
        verifyNoMoreInteractions(locationService);
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} - delete 不存在：BusinessException(404)")
    void delete_notFound() throws Exception {
        when(locationService.removeById(8L)).thenReturn(false);

        MvcResult result = mockMvc.perform(delete("/api/locations/8"))
                .andReturn();

        assertBizError(result, 404, "删除失败");

        verify(locationService, times(1)).removeById(8L);
        verifyNoMoreInteractions(locationService);
    }
}