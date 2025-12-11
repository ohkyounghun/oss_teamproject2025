import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { downloadReport } from "../api/checkMateApi.js";
import "./ResultPage.css";
import logo from "../assets/logo.png";

function ResultPage() {
    const { state } = useLocation();
    const navigate = useNavigate();
    const [selected, setSelected] = useState(null);

    const convertStatus = (status) => {
        switch (status) {
            case "FULFILLED": return "충족";
            case "PARTIAL": return "부분 충족";
            case "NOT_FULFILLED": return "미충족";
            default: return status;
        }
    };

    if (!state || !Array.isArray(state.results) || state.results.length === 0) {
        return (
            <div className="result-root">
                <nav className="navbar">
                    <div className="nav-left">
                        CheckMate
                    </div>
                </nav>

                <div className="result-container">
                    <div className="empty-card">
                        <h2>결과 데이터를 불러올 수 없습니다.</h2>
                        <p>업로드 화면에서 다시 분석을 실행해 주세요.</p>

                        <button className="restart-btn primary" onClick={() => navigate("/")}>
                            업로드 화면으로 돌아가기
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    const { results, requirementsFile, submissionFile } = state;

    const fulfilled = results.filter(r => r.status === "FULFILLED").length;
    const partial = results.filter(r => r.status === "PARTIAL").length;
    const notFulfilled = results.filter(r => r.status === "NOT_FULFILLED").length;

    const total = results.length;
    const finalScore =
        total > 0 ? ((fulfilled + partial * 0.5) / total) * 100 : 0;

    const handleDownload = async () => {
        try {
            const blob = await downloadReport(requirementsFile, submissionFile);
            const url = URL.createObjectURL(blob);

            const a = document.createElement("a");
            a.href = url;
            a.download = "CheckMate_Report.txt";
            a.click();
            URL.revokeObjectURL(url);
        } catch {
            alert("보고서 다운로드 중 오류가 발생했습니다.");
        }
    };

    const openDetail = (r) => {
        const evidence =
            r.evidence?.trim()
                ? r.evidence
                : r.reason?.trim()
                    ? r.reason
                    : "근거 없음";

        setSelected({ ...r, evidence });
    };

    return (
        <div className="result-root">
            {/* NAV */}
            <nav className="navbar">
                <div
                    className="nav-left"
                    onClick={() => navigate("/")}
                    style={{ cursor: "pointer" }}
                >
                    <img
                        src={logo}
                        alt="logo"
                        style={{ height: "110px" }}
                    />
                </div>
            </nav>

            <div className="result-container">

                <section className="result-hero">
                    <h1 className="result-title">분석 결과 요약</h1>
                    <p className="result-subtitle">요구사항 충족 여부와 근거를 확인해보세요.</p>
                    <div className="result-divider"></div>
                </section>

                <section className="summary-card">
                    <div className="summary-left">
                        <div className="summary-label">최종 점수</div>
                        <div className="summary-score">
                            {finalScore.toFixed(1)}
                            <span className="summary-score-unit">점</span>
                        </div>
                        <div className="summary-caption">(충족 1점, 부분 충족 0.5점 기준)</div>
                    </div>

                    <div className="summary-right">
                        <div className="summary-stat-row fulfilled">
                            <span className="dot"></span>
                            <span>충족</span>
                            <strong>{fulfilled}</strong>
                        </div>
                        <div className="summary-stat-row partial">
                            <span className="dot"></span>
                            <span>부분 충족</span>
                            <strong>{partial}</strong>
                        </div>
                        <div className="summary-stat-row not">
                            <span className="dot"></span>
                            <span>미충족</span>
                            <strong>{notFulfilled}</strong>
                        </div>

                        <button className="download-btn" onClick={handleDownload}>
                            리포트 다운로드
                        </button>
                    </div>
                </section>

                <section className="detail-header">
                    <h2 className="detail-title">요구사항별 상세 결과</h2>
                    <p className="detail-subtitle">
                        항목을 클릭하면 판정 근거를 확인할 수 있습니다.
                    </p>
                </section>

                <section className="result-list">
                    {results.map((r, idx) => (
                        <div
                            key={idx}
                            className="result-item"
                            onClick={() => openDetail(r)}
                        >
                            <div className="result-main">
                                <b className="requirement-text">{r.requirementText}</b>
                                <span className={`status-badge status-${r.status}`}>
                                    {convertStatus(r.status)}
                                </span>
                            </div>

                            <div className="result-preview">
                                {(r.evidence || r.reason || "근거 없음").slice(0, 90)}
                                {(r.evidence?.length > 90 || r.reason?.length > 90) ? " ..." : ""}
                            </div>
                        </div>
                    ))}
                </section>

                {selected && (
                    <div className="modal-bg" onClick={() => setSelected(null)}>
                        <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                            <h2 className="modal-title">근거 상세</h2>

                            <div className="modal-section">
                                <b>요구사항</b>
                                <p>{selected.requirementText}</p>
                            </div>

                            <div className="modal-section">
                                <b>판정</b>
                                <p>{convertStatus(selected.status)}</p>
                            </div>

                            <div className="modal-section">
                                <b>근거</b>
                                <p>{selected.evidence}</p>
                            </div>

                            <button className="modal-close" onClick={() => setSelected(null)}>
                                닫기
                            </button>
                        </div>
                    </div>
                )}

                <button className="restart-btn" onClick={() => navigate("/")}>
                    새로 분석하기
                </button>
            </div>

            {/* FOOTER */}
            <footer className="footer">
                <div className="footer-inner">
                    <div className="footer-col">
                        <h4>
                            CheckMate
                        </h4>
                        <p>AI 기반 요구사항 충족 분석 서비스</p>
                    </div>
                    <div className="footer-col">
                        <h4>Team</h4>
                        <p>권도훈 · 김건우 · 오경훈 · 임동현</p>
                    </div>
                    <div className="footer-col">
                        <h4>Contact</h4>
                        <p>ph. 010-6220-8271</p>
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default ResultPage;
