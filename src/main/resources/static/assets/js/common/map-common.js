/**
 * ============================================
 * 지니데이타 지도 공통 모듈 (Map Common)
 * ============================================
 * 
 * 🎯 지도 공통 기능 책임
 * ✅ 네이버 지도 초기화 및 설정
 * ✅ 마우스 이벤트 처리 (On/Off 가능)
 * ✅ 행정동 경계 표시 및 Point-in-Polygon 체크
 * ✅ 페이지별 커스터마이징 지원
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[MAP-COMMON] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 지도 공통 기능 ==============================
    Zinidata.map = {
        // 지도 관련 변수들
        map: null,
        currentPolygon: null,
        currentInfoWindow: null,
        currentSelectionMarker: null,
        currentSelectionMarkers:[],
        // hover 요청 제어는 throttle로 일원화
        throttledLoadAdmi: null,
        config: {},
        selectionLocked: false,

        // 지도 초기화 메인 함수
        init: function(options = {}) {
            const self = this;
            
            return new Promise((resolve, reject) => {
                // 기본 설정과 사용자 옵션 병합
                self.config = {
                    pageType: 'default',           // 페이지 타입 ('summary', 'flowpop', 'density' 등)
                    enableMouseTracking: false,    // 마우스 이동 추적 활성화
                    enableAdmiDisplay: false,      // 행정동 경계 표시 활성화
                    enableClickToDraw: true,       // 지도 클릭 시 행정동 그리기 활성화
                    enableUserLocation: true,      // 사용자 GPS 위치 기반 초기화 활성화
                    center: [37.531211, 126.914977],   // 지도 중심점 [lat, lng] (국회의사당)
                    zoom: 13,                      // 초기 줌 레벨
                    minZoom: 6,                    // 최소 줌 레벨
                    maxZoom: 21,                   // 최대 줌 레벨
                    debounceTime: 100,             // 마우스 이벤트 디바운스 시간 (ms)
                    requestInterval: 500,          // API 요청 최소 간격 (ms)
                    customEvents: [],              // 커스텀 이벤트 배열
                    useCustomControls: true,       // 커스텀 컨트롤 사용 여부(전 페이지 공통)
                    disableKineticPan : true,
                    ...options
                };

                console.log(`[MAP-COMMON] 지도 초기화 시작 - 페이지: ${self.config.pageType}`);
                
                // 초기화 완료 콜백 저장
                self._initResolve = resolve;
                self._initReject = reject;
                
                // 사용자 GPS 위치 기반 초기화가 활성화된 경우 위치 조회 후 지도 초기화
                if (self.config.enableUserLocation) {
                    self.initMapWithUserGPSLocation();
                } else {
                    self.initMap();
                }
            });
        },

        // 사용자 GPS 위치 기반 지도 초기화
        initMapWithUserGPSLocation: function() {
            const self = this;
            
            // HTML5 Geolocation API 사용
            if (navigator.geolocation) {
                console.log('[MAP-COMMON] GPS 위치 조회 시작...');
                
                navigator.geolocation.getCurrentPosition(
                    // 성공 시
                    function(position) {
                        const userLat = position.coords.latitude;
                        const userLng = position.coords.longitude;
                        const accuracy = position.coords.accuracy;
                        
                        console.log('[MAP-COMMON] 사용자 GPS 위치 조회 성공:', {
                            lat: userLat,
                            lng: userLng,
                            accuracy: accuracy + 'm'
                        });
                        
                        // 사용자 위치로 중심점 업데이트
                        self.config.center = [userLat, userLng];
                        
                        // 지도 초기화
                        self.initMap();
                    },
                    // 실패 시
                    function(error) {
                        console.warn('[MAP-COMMON] GPS 위치 조회 실패:', error.message);
                        console.log('[MAP-COMMON] 기본 위치로 지도 초기화');
                        self.initMap();
                    },
                    // 옵션
                    {
                        enableHighAccuracy: true,  // 높은 정확도
                        timeout: 10000,            // 10초 타임아웃
                        maximumAge: 60000          // 1분 이내 캐시된 위치 허용
                    }
                );
            } else {
                console.log('[MAP-COMMON] GPS 위치 조회를 지원하지 않는 브라우저, 기본 위치로 초기화');
                self.initMap();
            }
        },

        // 네이버 지도 초기화
        initMap: function() {
            const self = this;
            
            function createMap() {
                if (typeof naver !== 'undefined' && naver.maps) {
                    // 페이지별 지도 옵션 설정
                    const mapOptions = self.getMapOptions();
                    
                    // 지도 생성
                    self.map = new naver.maps.Map('mapContainer', mapOptions);
                    
                    // 지도 타입 고정 (일반 지도로 고정)
                    if (self.config.mapTypeControl === false) {
                        self.map.setMapTypeId(naver.maps.MapTypeId.NORMAL);
                        // 지도 타입 변경 이벤트 차단
                        naver.maps.Event.addListener(self.map, 'maptypeid_changed', function() {
                            if (self.map.getMapTypeId() !== naver.maps.MapTypeId.NORMAL) {
                                self.map.setMapTypeId(naver.maps.MapTypeId.NORMAL);
                            }
                        });
                    }
                    
                    // 이벤트 설정
                    self.setupEvents();

                    // 초기화 완료 로그
                    console.log(`[MAP-COMMON] 네이버 지도 초기화 완료 - ${self.config.pageType} 페이지`);
                    
                    // 커스텀 초기화 콜백 실행
                    if (typeof window.onMapInitialized === 'function') {
                        window.onMapInitialized(self.map);
                    }
                    // 모든 페이지에서 커스텀 컨트롤 사용 (옵션으로 제어)
                    if (self.config.useCustomControls && typeof self.createSummaryControls === 'function') {
                        try { self.createSummaryControls(); } catch (e) { console.warn('[MAP-COMMON] 커스텀 컨트롤 생성 오류:', e); }
                    }
                    
                    // Promise resolve (초기화 완료)
                    if (self._initResolve) {
                        self._initResolve(self.map);
                        self._initResolve = null;
                        self._initReject = null;
                    }
                } else {
                    // API가 아직 로드되지 않았으면 100ms 후 재시도
                    setTimeout(createMap, 100);
                }
            }
            
            createMap();
        },

        // 페이지별 지도 옵션 설정
        getMapOptions: function() {
            const baseOptions = {
                center: new naver.maps.LatLng(this.config.center[0], this.config.center[1]),
                zoom: this.config.zoom,
                zoomControl: this.config.zoomControl !== undefined ? this.config.zoomControl : (this.config.useCustomControls ? false : true),
                zoomControlOptions: {
                    style: naver.maps.ZoomControlStyle.SMALL,
                    position: naver.maps.Position.BOTTOM_RIGHT
                },
                mapTypeControl: this.config.mapTypeControl !== undefined ? this.config.mapTypeControl : (this.config.useCustomControls ? false : true),
                mapTypeControlOptions: {
                    style: naver.maps.MapTypeControlStyle.BUTTON,
                    position: naver.maps.Position.BOTTOM_RIGHT
                },
                scaleControl: this.config.scaleControl !== undefined ? this.config.scaleControl : false,
                logoControl: this.config.logoControl !== undefined ? this.config.logoControl : true,
                logoControlOptions: {
                    position: naver.maps.Position.BOTTOM_RIGHT
                },
                mapDataControl: this.config.mapDataControl !== undefined ? this.config.mapDataControl : false,
                minZoom: this.config.minZoom,
                maxZoom: this.config.maxZoom
            };

            // 페이지별 특화 설정
            switch (this.config.pageType) {
                case 'summary':
                    return {
                        ...baseOptions,
                        // 종합보고서 특화 설정
                        zoom: 13,
                        minZoom: 6,
                        maxZoom: 21
                    };
                
                case 'flowpop':
                    return {
                        ...baseOptions,
                        // 유동인구 특화 설정 (PC/모바일 구분)
                        center: new naver.maps.LatLng(37.531211, 126.914977),
                        zoom: (typeof window !== 'undefined' && window.isMobile) ? 12 : 15, // PC: 15, 모바일: 14
                        minZoom: 8,
                        maxZoom: 19
                    };
                
                case 'density':
                    return {
                        ...baseOptions,
                        // 점포밀집도 특화 설정 (PC/모바일 구분)
                        center: new naver.maps.LatLng(37.531211, 126.914977),
                        zoom: (typeof window !== 'undefined' && window.isMobile) ? 12 : 15, // PC: 15, 모바일: 12
                        minZoom: 8,
                        maxZoom: 19
                    };
                
                default:
                    return baseOptions;
            }
        },

        // 이벤트 설정
        setupEvents: function() {
            // 기존 이벤트 리스너 모두 제거 (중복 방지)
            if (this.map && this._clickListener) {
                naver.maps.Event.removeListener(this._clickListener);
            }
            
            // 기본 클릭 이벤트 (옵션에 따라 활성화)
            if (this.config.enableClickToDraw) {
                let lastClickTime = 0;
                this._clickListener = naver.maps.Event.addListener(this.map, 'click', (e) => {
                    const now = Date.now();
                    // 300ms 내 중복 클릭 방지
                    if (now - lastClickTime < 300) {
                        console.log('[MAP-COMMON] 중복 클릭 방지:', now - lastClickTime + 'ms');
                        return;
                    }
                    lastClickTime = now;
                    
                    const lat = e.coord.lat();
                    const lng = e.coord.lng();
                    console.log('[MAP-COMMON] 지도 클릭:', lat, lng);
                    this.selectRegionByPoint(lat, lng);
                    try { if (typeof window.showMobileSearchPanel === 'function') window.showMobileSearchPanel(); } catch (_) {}
                    if (typeof window.onMapClick === 'function') {
                        window.onMapClick(lat, lng);
                    }
                });
            }

            // 마우스 이동 이벤트 (옵션에 따라 활성화)
            if (this.config.enableMouseTracking) {
                this.setupMouseTracking();
            }

            // 커스텀 이벤트 설정
            this.setupCustomEvents();
        },

        // 마우스 추적 이벤트 설정
        setupMouseTracking: function() {
            // 스로틀 함수 초기화(요청 최소 간격은 config.requestInterval 기준)
            this.throttledLoadAdmi = Zinidata.performance.debounce((lat, lng) => {
                this.loadAdmiDistrict(lat, lng);
            }, this.config.requestInterval);

            naver.maps.Event.addListener(this.map, 'mousemove', (e) => {
                this.handleMouseMove(e.coord.lat(), e.coord.lng());
            });
            
            console.log('[MAP-COMMON] 마우스 추적 이벤트 활성화');
        },

        // 커스텀 이벤트 설정
        setupCustomEvents: function() {
            const customEvents = this.config.customEvents || [];
            
            customEvents.forEach(eventName => {
                naver.maps.Event.addListener(this.map, eventName, (e) => {
                    // 커스텀 이벤트 콜백 실행
                    const callbackName = `onMap${eventName.charAt(0).toUpperCase() + eventName.slice(1)}`;
                    if (typeof window[callbackName] === 'function') {
                        window[callbackName](e);
                    }
                });
            });
        },

        // 마우스 이동 처리
        handleMouseMove: function(lat, lng) {
            // 현재 마우스 위치 저장
            window.currentMousePos = new naver.maps.LatLng(lat, lng);
            
            // 행정동 표시가 활성화된 경우에만 처리 + 선택 확정 시 중단
            if (!this.config.enableAdmiDisplay || this.selectionLocked) return;
            
            // 현재 폴리곤 내부에 있으면 DB 조회 안함
            if (this.currentPolygon && this.isPointInPolygon(lat, lng, this.currentPolygon)) {
                return; // DB 조회 없이 바로 리턴
            }
            
            // 폴리곤 외부거나 폴리곤이 없으면 스로틀된 DB 조회 호출
            if (typeof this.throttledLoadAdmi === 'function') {
                this.throttledLoadAdmi(lat, lng);
            } else {
                // 가드: 스로틀 초기화 전이라면 즉시 호출(초기 로드 구간 보호)
                this.loadAdmiDistrict(lat, lng);
            }
        },

        // Point-in-Polygon 체크 (Ray Casting Algorithm)
        isPointInPolygon: function(lat, lng, polygon) {
            if (!polygon) return false;
            
            try {
                const point = new naver.maps.LatLng(lat, lng);
                const paths = polygon.getPaths();
                
                if (!paths || paths.length === 0) return false;
                
                // MultiPolygon 대응 - 모든 path 확인
                for (let i = 0; i < paths.length; i++) {
                    const path = paths.getAt(i);
                    if (this.pointInPolygonPath(point, path)) {
                        return true;
                    }
                }
                return false;
            } catch (e) {
                return false;
            }
        },

        // Ray Casting으로 점이 경로 내부에 있는지 판단
        pointInPolygonPath: function(point, path) {
            const x = point.lng();
            const y = point.lat();
            let inside = false;
            
            const len = path.getLength();
            for (let i = 0, j = len - 1; i < len; j = i++) {
                const xi = path.getAt(i).lng();
                const yi = path.getAt(i).lat();
                const xj = path.getAt(j).lng();
                const yj = path.getAt(j).lat();
                
                if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                    inside = !inside;
                }
            }
            
            return inside;
        },

        // 행정동 데이터 조회 및 경계 표시
        loadAdmiDistrict: function(lat, lng) {
            // API 호출 (요청 빈도 제어는 상위 throttledLoadAdmi에서 수행)
			try {
				Zinidata.api({
					url: `/api/common/region/admi/by-point?lat=${lat}&lng=${lng}`,
					method: 'GET',
					success: (data) => {
						if (data.success && data.data) {
							this.displayAdmiRegion(data.data);
						} else {
                            // 행정동이 없는 경우 기존 경계 제거
							this.clearCurrentPolygon();
						}
					},
					error: () => {
						this.clearCurrentPolygon();
					}
				});
			} catch (_) {
				this.clearCurrentPolygon();
			}
        },

        // 클릭 위치의 행정구역을 확정 선택하고 이후 마우스 이동에 반응하지 않음
        selectRegionByPoint: function(lat, lng) {
            const self = this;
			self.selectionLocked = true;
			
			try {
				Zinidata.api({
					url: `/api/common/region/admi/by-point?lat=${lat}&lng=${lng}`,
					method: 'GET',
					success: (data) => {
						if (data && data.success && data.data) {
							const admiData = data.data;
							self.displayAdmiRegion(admiData);
                            // UI에 선택 지역 반영
                            // 분석지역 표시용 풀 라벨: mega_nm + cty_nm + admi_nm
							const fullName = [admiData.megaNm, admiData.ctyNm, admiData.admiNm]
								.filter(Boolean)
								.join(' ');
							const name = fullName || admiData.admiNm || admiData.Admim || admiData.admiName || admiData.name || '';
							let admiCd = admiData.admiCd || admiData.Admicd || '';
							if (admiCd && admiCd.length === 10) {
								admiCd = admiCd.substring(0, 8);
							}
							if (typeof window.onRegionSelected === 'function') {
								window.onRegionSelected({
									name: name,
									admiCd: admiCd,
									coordinates: { lat, lng },
									code: admiCd,
									fromMapClick: true,
									admiData: admiData  // 이미 받은 API 데이터 전달
								});
							}
						}
					},
					error: () => { 
						/* 선택 잠금 유지 */ 
					}
				});
			} catch (_) { 
				/* 선택 잠금 유지 */ 
			}
        },

        // 행정동 경계 표시
        displayAdmiRegion: function(admiData) {
            try {
                // 기존 경계 및 InfoWindow 제거
                this.clearCurrentPolygon();
                
                // GeoJSON 파싱
                const feature = JSON.parse(admiData.feature);
                const geometry = feature.geometry;
                
                if (geometry.type === 'MultiPolygon') {
                    // MultiPolygon 처리
                    const paths = [];
                    geometry.coordinates.forEach(polygon => {
                        polygon.forEach(ring => {
                            const path = ring.map(coord => new naver.maps.LatLng(coord[1], coord[0]));
                            paths.push(path);
                        });
                    });
                    
                    // 폴리곤 생성 및 표시
                    this.currentPolygon = new naver.maps.Polygon({
                        map: this.map,
                        paths: paths,
                        fillColor: '#0066cc',
                        fillOpacity: 0.2,
                        strokeColor: '#0066cc',
                        strokeOpacity: 0.8,
                        strokeWeight: 2
                    });
                    
                    // InfoWindow는 사용하지 않음 (hover/선택 모두 불필요)
                    
                    // 커스텀 행정동 표시 콜백
                    if (typeof window.onAdmiDisplayed === 'function') {
                        window.onAdmiDisplayed(admiData, this.currentPolygon);
                    }
                }
                
            } catch (error) {
                console.error('[MAP-COMMON] 행정동 경계 표시 오류:', error);
            }
        },

        // 현재 표시된 경계 및 InfoWindow 제거
        clearCurrentPolygon: function() {
            if (this.currentPolygon) {
                this.currentPolygon.setMap(null);
                this.currentPolygon = null;
            }
            // InfoWindow는 사용하지 않으므로 정리 로직 유지만 함
            // 폴리곤 제거 시에도 선택 마커는 유지 (선택 유지 UX). 필요 시 아래 주석 해제
            // this.clearSelectionMarker();
        },

        // 지도 인스턴스 반환
        getMap: function() {
            return this.map;
        },

        // 현재 폴리곤 반환
        getCurrentPolygon: function() {
            return this.currentPolygon;
        },

        // 설정 업데이트
        updateConfig: function(newOptions) {
            this.config = { ...this.config, ...newOptions };
        },

        // ============================== 선택 마커 표시/제거 ==============================
        /**
         * 선택한 위치에 라벨 마커 표시
         * @param {number} lat
         * @param {number} lng
         * @param {string} label
         */
        showSelectionMarker: function(lat, lng, label) {
            const map = this.getMap();
            if (!map || typeof naver === 'undefined' || !naver.maps) return;
            this.clearSelectionMarker();

            const position = new naver.maps.LatLng(lat, lng);
            const safeLabel = String(label || '').replace(/&/g, '&amp;').replace(/</g, '&lt;')
                .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');

            // 프로젝트의 마커 템플릿(HTML) 구조를 그대로 사용
            const content = `
                <div class="selectionMarkerWrapper">
                    <div class="mapMarker mapMarkerOnMap">
                        <img src="/assets/images/icons/map_marker.svg" alt="지도 마커" />
                        <div class="mapMarkerText"><p>${safeLabel}</p></div>
                    </div>
                </div>`;

            this.currentSelectionMarker = new naver.maps.Marker({
                position: position,
                map: map,
                icon: {
                    content: content,
                    size: new naver.maps.Size(0, 0),
                    anchor: new naver.maps.Point(0, 0)
                }
            });
        },

        clearSelectionMarker: function() {
            if (this.currentSelectionMarker) {
                this.currentSelectionMarker.setMap(null);
                this.currentSelectionMarker = null;
            }
        },

        // ============================== 목록 마커 표시/제거 ==============================
        /**
         * 목록 마커 표시
         * @param {number} lat
         * @param {number} lng
         * @param {string} label
         */
        markerList: function(data, contentHtml) {
            const self = this;
            const map = this.getMap();
            if (!map || typeof naver === 'undefined' || !naver.maps) return;
            
            // currentSelectionMarkers 배열 초기화 확인
            if (!self.currentSelectionMarkers) {
                self.currentSelectionMarkers = [];
            }
            
            this.clearMarkerList();

            data.forEach(function (val, idx){
                // 좌표 확인 (centerY, centerX 또는 lat, lng 사용)
                const lat = val.lat || val.centerY;
                const lng = val.lng || val.centerX;
                
                if (!lat || !lng) {
                    console.warn('[MAP-COMMON] 마커 좌표가 없습니다:', val);
                    return;
                }
                
                const position = new naver.maps.LatLng(lat, lng);
    
                const marker = new naver.maps.Marker({
                    position: position,
                    map: map,
                    icon: {
                        content: contentHtml[idx],
                        size: new naver.maps.Size(0, 0),
                        anchor: new naver.maps.Point(0, 0)
                    }
                });

                self.currentSelectionMarkers.push(marker);
            });
        },

        /**
         * 목록 마커 제거
         */
        clearMarkerList: function() {
            if (!this.currentSelectionMarkers || !Array.isArray(this.currentSelectionMarkers)) {
                this.currentSelectionMarkers = [];
                return;
            }
            
            this.currentSelectionMarkers.forEach(function (marker){
                if (marker && marker.setMap) {
                    marker.setMap(null);
                }
            });
            this.currentSelectionMarkers = [];
        },

        // 줌 레벨에 따른 구분 반환
        getZoomGubun: function() {
            if (!this.map) {
                console.warn('[MAP-COMMON] 지도가 초기화되지 않았습니다.');
                return null;
            }

            const zoom = this.map.getZoom();

            if (zoom >= 15) {
                return 'block';
            } else if (zoom === 14) {
                return 'admi';
            } else if (zoom === 13 || zoom === 12) {
                return 'cty';
            } else if (zoom <= 11) {
                return 'mega';
            }
        }
    };
});