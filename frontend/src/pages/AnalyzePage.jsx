import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState, useRef } from "react";
import { analyzeRequest } from "../api/checkMateApi.js";
import logo from "../assets/logo.png";
import "./AnalyzePage.css";

function AnalyzePage() {
    const { state } = useLocation();
    const navigate = useNavigate();

    const [activeStep, setActiveStep] = useState(0);
    const [completedStep, setCompletedStep] = useState(0);
    const [dotCount, setDotCount] = useState(1);

    const startedRef = useRef(false);

    const requirementsFile = state?.requirementsFile;
    const submissionFile = state?.submissionFile;

    const stepTexts = [
        "요구사항을 분석하는 중",
        "제출 문서를 분석하는 중",
        "검증 보고서를 정리하는 중"
    ];

    useEffect(() => {
        if (!requirementsFile || !submissionFile) {
            navigate("/");
            return;
        }

        if (startedRef.current) return;
        startedRef.current = true;

        // 점 애니메이션
        const dotInterval = setInterval(() => {
            setDotCount(prev => (prev === 3 ? 1 : prev + 1));
        }, 450);

        const timers = [];
        timers.push(setTimeout(() => setActiveStep(1), 300));
        timers.push(setTimeout(() => {
            setCompletedStep(1);
            setActiveStep(2);
        }, 1500));
        timers.push(setTimeout(() => {
            setCompletedStep(2);
            setActiveStep(3);
        }, 2700));

        // 실제 API 호출
        (async () => {
            try {
                const apiResult = await analyzeRequest(requirementsFile, submissionFile);

                let results = [];
                if (Array.isArray(apiResult)) results = apiResult;
                else if (apiResult?.details) results = apiResult.details;

                setCompletedStep(3);

                setTimeout(() => {
                    navigate("/result", {
                        state: { results, requirementsFile, submissionFile }
                    });
                }, 700);

            } catch (err) {
                console.error(err);
                alert("분석 중 오류가 발생했습니다.");
                navigate("/");
            }
        })();

        return () => {
            timers.forEach(clearTimeout);
            clearInterval(dotInterval);
        };
    }, []);

    return (
        <div className="analyze-root">

            {/* NAVBAR — 업로드 페이지와 동일 */}
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

            {/* 중앙 컨텐츠 */}
            <div className="analyze-container">

                <div className="spinner"></div>

                <h1 className="loading-title">문서를 분석하고 있습니다...</h1>

                <div className="steps-wrapper">
                    {stepTexts.map((txt, i) => {
                        const step = i + 1;
                        const isActive = activeStep === step;
                        const isCompleted = completedStep >= step;

                        const animated =
                            isActive && !isCompleted
                                ? txt + ".".repeat(dotCount)
                                : txt;
                        return (
                            <div
                                key={i}
                                className={`step-box
                                    ${isActive ? "active" : ""}
                                    ${isCompleted ? "completed" : ""}
                                `}
                            >
                                {animated}

                                {isCompleted && (
                                    <div className="check-circle">✔</div>
                                )}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}

export default AnalyzePage;
