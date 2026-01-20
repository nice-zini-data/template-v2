/**
 * 지니데이타 파일 처리 모듈 (File Module)
 * 
 * 파일 관련 모든 기능들을 제공합니다:
 * - 파일 검증 (크기, 타입)
 * - 파일 미리보기 및 업로드
 * - 보안 파일 다운로드 (Presigned URL)
 * - 드래그앤드롭 파일 처리
 * - 파일 다운로드 진행 상태 표시
 * 
 * 의존성: zinidata-core.js가 먼저 로드되어야 합니다.
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */

$(document).ready(function() {
    // 의존성 체크
    if (typeof Zinidata === 'undefined') {
        console.error('파일 다운로드 모듈 의존성 오류: zinidata-core.js가 로드되지 않았습니다.');
        return;
    }

    // 파일 모듈 활성화
    Zinidata.modules.file = true;
    
    // 파일 처리 유틸리티 (기본 기능)
    Zinidata.file = {
        /**
         * 파일 크기 검증
         */
        validateSize: function(file, maxSizeMB) {
            if (!file) return false;
            var maxSizeBytes = maxSizeMB * 1024 * 1024;
            return file.size <= maxSizeBytes;
        },
        
        /**
         * 파일 타입 검증
         */
        validateType: function(file, allowedTypes) {
            if (!file) return false;
            if (typeof allowedTypes === 'string') {
                allowedTypes = [allowedTypes];
            }
            return allowedTypes.includes(file.type);
        },
        
        /**
         * 이미지 미리보기
         */
        previewImage: function(file, targetElementId) {
            if (!file || !file.type.startsWith('image/')) {
                return;
            }
            
            var reader = new FileReader();
            reader.onload = function(e) {
                $('#' + targetElementId).attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
        },
        
        /**
         * 파일 다운로드 (Blob)
         */
        downloadBlob: function(blob, filename) {
            var url = window.URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        },
        
        /**
         * 드래그 앤 드롭 파일 업로드 설정
         */
        setupDragDrop: function(targetElementId, options) {
            options = $.extend({
                allowedTypes: null,
                maxSizeMB: 10,
                onFileSelect: null,
                onError: null
            }, options);
            
            var $target = $('#' + targetElementId);
            
            $target.on('dragover dragenter', function(e) {
                e.preventDefault();
                e.stopPropagation();
                $(this).addClass('drag-over');
            });
            
            $target.on('dragleave dragend', function(e) {
                e.preventDefault();
                e.stopPropagation();
                $(this).removeClass('drag-over');
            });
            
            $target.on('drop', function(e) {
                e.preventDefault();
                e.stopPropagation();
                $(this).removeClass('drag-over');
                
                var files = e.originalEvent.dataTransfer.files;
                this.handleFileSelection(files, options);
            }.bind(this));
        },
        
        /**
         * 파일 선택 처리
         */
        handleFileSelection: function(files, options) {
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                
                // 파일 타입 검증
                if (options.allowedTypes && !this.validateType(file, options.allowedTypes)) {
                    if (options.onError) {
                        options.onError('허용되지 않는 파일 타입입니다: ' + file.type);
                    }
                    continue;
                }
                
                // 파일 크기 검증
                if (!this.validateSize(file, options.maxSizeMB)) {
                    if (options.onError) {
                        options.onError('파일 크기가 ' + options.maxSizeMB + 'MB를 초과합니다.');
                    }
                    continue;
                }
                
                // 파일 선택 콜백
                if (options.onFileSelect) {
                    options.onFileSelect(file);
                }
            }
        }
    };
    
    // 파일 다운로드 전용 네임스페이스
    Zinidata.fileDownload = {
        
        /**
         * 보안 파일 다운로드
         * 
         * @param {number} fileId 파일 ID
         * @param {string} fileName 파일명 (선택사항)
         * @param {object} options 다운로드 옵션
         */
        downloadFile: async function(fileId, fileName, options) {
            options = $.extend({
                showProgress: true,
                autoRetry: false,
                retryCount: 1,
                onStart: null,
                onSuccess: null,
                onError: null,
                onComplete: null
            }, options);
            
            console.log('파일 다운로드 시작:', { fileId, fileName, options });
            
            let downloadId = null;
            
            try {
                // 1. 다운로드 시작 콜백
                if (options.onStart && typeof options.onStart === 'function') {
                    options.onStart(fileId, fileName);
                }
                
                // 2. 로딩 시작
                if (options.showProgress) {
                    this._showLoading('파일 다운로드 URL 생성 중...');
                }
                
                // 3. Presigned URL 요청
                const urlResponse = await this._requestDownloadUrl(fileId);
                
                if (!urlResponse.success) {
                    console.error('[FILE-DOWNLOAD] 다운로드 URL 생성 실패:', urlResponse);
                    return;
                }
                
                const downloadData = urlResponse.data;
                downloadId = downloadData.downloadId;
                fileName = fileName || downloadData.fileName;
                
                // 4. 다운로드 실행 (5초 만료 대비 즉시 실행)
                if (options.showProgress) {
                    this._updateLoading('파일 다운로드 시작 중...');
                }
                
                await this._executeDownload(downloadData.downloadUrl, fileName);
                
                // 5. 다운로드 완료 알림
                await this._notifyDownloadComplete(fileId, downloadId);
                
                // 6. 성공 처리
                if (options.showProgress) {
                    this._showSuccess(`${fileName} 다운로드가 완료되었습니다.`);
                }
                
                if (options.onSuccess && typeof options.onSuccess === 'function') {
                    options.onSuccess(fileId, fileName, downloadData);
                }
                
                console.log('파일 다운로드 완료:', { fileId, fileName, downloadId });
                
            } catch (error) {
                console.error('파일 다운로드 오류:', error);
                
                // 자동 재시도
                if (options.autoRetry && options.retryCount > 0) {
                    console.log('파일 다운로드 재시도:', { fileId, retryCount: options.retryCount });
                    options.retryCount--;
                    setTimeout(() => {
                        this.downloadFile(fileId, fileName, options);
                    }, 1000);
                    return;
                }
                
                // 에러 처리
                const errorMessage = this._getErrorMessage(error);
                
                if (options.showProgress) {
                    this._showError(errorMessage);
                }
                
                if (options.onError && typeof options.onError === 'function') {
                    options.onError(fileId, fileName, error);
                }
                
            } finally {
                // 완료 처리
                if (options.showProgress) {
                    this._hideLoading();
                }
                
                if (options.onComplete && typeof options.onComplete === 'function') {
                    options.onComplete(fileId, fileName);
                }
            }
        },
        
        /**
         * Sales 파일 다운로드 (년월 기반)
         * 
         * @param {string} startMonth 시작년월 (YYYYMM)
         * @param {string} endMonth 종료년월 (YYYYMM)  
         * @param {object} options 다운로드 옵션
         */
        downloadSalesFile: async function(startMonth, endMonth, options) {
            options = $.extend({
                showProgress: true,
                autoRetry: false,
                retryCount: 1,
                onStart: null,
                onSuccess: null,
                onError: null,
                onComplete: null
            }, options);
            
            console.log('Sales 파일 다운로드 시작:', { startMonth, endMonth, options });
            
            let downloadId = null;
            
            try {
                // 1. 입력값 검증
                if (!this._isValidYearMonth(startMonth) || !this._isValidYearMonth(endMonth)) {
                    console.error('[FILE-DOWNLOAD] 년월 형식이 올바르지 않습니다. YYYYMM 형식으로 입력하세요.');
                    return;
                }
                
                const fileName = `sales_${startMonth}_${endMonth}.xlsx`;
                
                // 2. 다운로드 시작 콜백
                if (options.onStart && typeof options.onStart === 'function') {
                    options.onStart(startMonth, endMonth, fileName);
                }
                
                // 3. 로딩 시작
                if (options.showProgress) {
                    this._showLoading('Sales 파일 다운로드 URL 생성 중...');
                }
                
                // 4. Presigned URL 요청
                const urlResponse = await this._requestSalesDownloadUrl(startMonth, endMonth);
                
                if (!urlResponse.success) {
                    console.error('[FILE-DOWNLOAD] Sales 파일 다운로드 URL 생성 실패:', urlResponse);
                    return;
                }
                
                const downloadData = urlResponse.data;
                downloadId = downloadData.downloadId;
                
                // 5. 다운로드 실행 (5초 만료 대비 즉시 실행)
                if (options.showProgress) {
                    this._updateLoading('Sales 파일 다운로드 시작 중...');
                }
                
                await this._executeDownload(downloadData.downloadUrl, downloadData.fileName);
                
                // 6. 성공 처리
                if (options.showProgress) {
                    this._showSuccess(`${downloadData.fileName} 다운로드가 완료되었습니다.`);
                }
                
                if (options.onSuccess && typeof options.onSuccess === 'function') {
                    options.onSuccess(startMonth, endMonth, downloadData);
                }
                
                console.log('Sales 파일 다운로드 완료:', { startMonth, endMonth, downloadId });
                
            } catch (error) {
                console.error('Sales 파일 다운로드 오류:', error);
                
                // 자동 재시도
                if (options.autoRetry && options.retryCount > 0) {
                    console.log('Sales 파일 다운로드 재시도:', { startMonth, endMonth, retryCount: options.retryCount });
                    options.retryCount--;
                    setTimeout(() => {
                        this.downloadSalesFile(startMonth, endMonth, options);
                    }, 1000);
                    return;
                }
                
                // 에러 처리
                const errorMessage = this._getErrorMessage(error);
                
                if (options.showProgress) {
                    this._showError(errorMessage);
                }
                
                if (options.onError && typeof options.onError === 'function') {
                    options.onError(startMonth, endMonth, error);
                }
                
            } finally {
                // 완료 처리
                if (options.showProgress) {
                    this._hideLoading();
                }
                
                if (options.onComplete && typeof options.onComplete === 'function') {
                    options.onComplete(startMonth, endMonth);
                }
            }
        },
        
        /**
         * 여러 파일 일괄 다운로드
         * 
         * @param {Array} fileList 파일 목록 [{fileId, fileName}, ...]
         * @param {object} options 다운로드 옵션
         */
        downloadMultiple: async function(fileList, options) {
            options = $.extend({
                sequential: true,  // 순차 다운로드 여부
                showProgress: true,
                onProgress: null,
                onAllComplete: null
            }, options);
            
            console.log('여러 파일 다운로드 시작:', { fileList, options });
            
            if (!Array.isArray(fileList) || fileList.length === 0) {
                console.error('[FILE-DOWNLOAD] 다운로드할 파일이 없습니다.');
                return;
            }
            
            const results = [];
            const totalFiles = fileList.length;
            let completedFiles = 0;
            
            try {
                if (options.showProgress) {
                    this._showLoading(`0/${totalFiles} 파일 다운로드 중...`);
                }
                
                if (options.sequential) {
                    // 순차 다운로드
                    for (const file of fileList) {
                        try {
                            await this.downloadFile(file.fileId, file.fileName, {
                                showProgress: false,
                                onSuccess: () => {
                                    completedFiles++;
                                    if (options.showProgress) {
                                        this._updateLoading(`${completedFiles}/${totalFiles} 파일 다운로드 중...`);
                                    }
                                    if (options.onProgress) {
                                        options.onProgress(completedFiles, totalFiles, file);
                                    }
                                }
                            });
                            results.push({ fileId: file.fileId, success: true });
                        } catch (error) {
                            results.push({ fileId: file.fileId, success: false, error: error.message });
                        }
                    }
                } else {
                    // 병렬 다운로드
                    const promises = fileList.map(async (file) => {
                        try {
                            await this.downloadFile(file.fileId, file.fileName, {
                                showProgress: false,
                                onSuccess: () => {
                                    completedFiles++;
                                    if (options.showProgress) {
                                        this._updateLoading(`${completedFiles}/${totalFiles} 파일 다운로드 중...`);
                                    }
                                    if (options.onProgress) {
                                        options.onProgress(completedFiles, totalFiles, file);
                                    }
                                }
                            });
                            return { fileId: file.fileId, success: true };
                        } catch (error) {
                            return { fileId: file.fileId, success: false, error: error.message };
                        }
                    });
                    
                    const promiseResults = await Promise.allSettled(promises);
                    results.push(...promiseResults.map(result => result.value));
                }
                
                // 완료 처리
                const successCount = results.filter(r => r.success).length;
                const failCount = results.filter(r => !r.success).length;
                
                if (options.showProgress) {
                    if (failCount === 0) {
                        this._showSuccess(`${successCount}개 파일 다운로드가 완료되었습니다.`);
                    } else {
                        this._showWarning(`${successCount}개 파일 다운로드 완료, ${failCount}개 파일 실패`);
                    }
                }
                
                if (options.onAllComplete) {
                    options.onAllComplete(results, successCount, failCount);
                }
                
                console.log('여러 파일 다운로드 완료:', { results, successCount, failCount });
                
            } finally {
                if (options.showProgress) {
                    this._hideLoading();
                }
            }
        },
        
        // ========== Private 메서드 ==========
        
        /**
         * 다운로드 URL 요청
         * 
         * @param {number} fileId 파일 ID
         * @returns {Promise<object>} 응답 데이터
         */
        _requestDownloadUrl: async function(fileId) {
            const response = await fetch(`/api/files/${fileId}/download-url`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            });
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.error('[FILE-DOWNLOAD] HTTP Error:', errorData.message || `HTTP ${response.status}: ${response.statusText}`);
                return;
            }
            
            return await response.json();
        },

        /**
         * Sales 파일 다운로드 URL 요청
         * 
         * @param {string} startMonth 시작년월 (YYYYMM)
         * @param {string} endMonth 종료년월 (YYYYMM)
         * @returns {Promise<object>} 응답 데이터
         */
        _requestSalesDownloadUrl: async function(startMonth, endMonth) {
            const response = await fetch(`/api/files/sales/download-url?startMonth=${startMonth}&endMonth=${endMonth}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.error('[FILE-DOWNLOAD] HTTP Error:', errorData.message || `HTTP ${response.status}: ${response.statusText}`);
                return;
            }

            return await response.json();
        },
        
        /**
         * 파일 다운로드 실행
         * 
         * @param {string} downloadUrl 다운로드 URL
         * @param {string} fileName 파일명
         */
        _executeDownload: function(downloadUrl, fileName) {
            return new Promise((resolve, reject) => {
                try {
                    // 다운로드 링크 생성 및 클릭
                    const link = document.createElement('a');
                    link.href = downloadUrl;
                    link.download = fileName;
                    link.target = '_blank';
                    link.style.display = 'none';
                    
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                    
                    // 다운로드 시작으로 간주 (브라우저에서 실제 완료 감지 불가)
                    setTimeout(resolve, 500);
                    
                } catch (error) {
                    reject(error);
                }
            });
        },
        
        /**
         * 다운로드 완료 알림
         * 
         * @param {number} fileId 파일 ID
         * @param {string} downloadId 다운로드 ID
         */
        _notifyDownloadComplete: async function(fileId, downloadId) {
            try {
                await fetch(`/api/files/${fileId}/download-complete?downloadId=${downloadId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    credentials: 'same-origin'
                });
            } catch (error) {
                console.warn('다운로드 완료 알림 실패:', error);
                // 알림 실패는 중요하지 않으므로 에러를 던지지 않음
            }
        },
        
        /**
         * 에러 메시지 정리
         * 
         * @param {Error} error 에러 객체
         * @returns {string} 정리된 에러 메시지
         */
        _getErrorMessage: function(error) {
            if (error.message) {
                return error.message;
            }
            
            if (typeof error === 'string') {
                return error;
            }
            
            return '파일 다운로드 중 알 수 없는 오류가 발생했습니다.';
        },

        /**
         * 년월 형식 유효성 검사
         * 
         * @param {string} yearMonth 년월 문자열 (YYYYMM)
         * @returns {boolean} 유효한 년월 형식인지 여부
         */
        _isValidYearMonth: function(yearMonth) {
            if (typeof yearMonth !== 'string') {
                return false;
            }
            return /^[0-9]{6}$/.test(yearMonth);
        },
        
        // UI 피드백 메서드들 (Zinidata.ui 의존성)
        _showLoading: function(message) {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.showLoading) {
                Zinidata.ui.showLoading(message);
            } else {
                console.log('로딩:', message);
            }
        },
        
        _updateLoading: function(message) {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.updateLoading) {
                Zinidata.ui.updateLoading(message);
            } else {
                console.log('로딩 업데이트:', message);
            }
        },
        
        _hideLoading: function() {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.hideLoading) {
                Zinidata.ui.hideLoading();
            }
        },
        
        _showSuccess: function(message) {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.showSuccess) {
                Zinidata.ui.showSuccess(message);
            } else {
                alert('성공: ' + message);
            }
        },
        
        _showError: function(message) {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.showError) {
                Zinidata.ui.showError(message);
            } else {
                alert('오류: ' + message);
            }
        },
        
        _showWarning: function(message) {
            if (typeof Zinidata.ui !== 'undefined' && Zinidata.ui.showWarning) {
                Zinidata.ui.showWarning(message);
            } else {
                alert('경고: ' + message);
            }
        }
    };
    
    console.log('ZiniData 파일 다운로드 모듈 초기화 완료');
});