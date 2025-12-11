import {useState} from "react";
import {useNavigate} from "react-router-dom";
import logo from "../assets/logo.png";
import logo_main from "../assets/logo_main.png";
import "./UploadPage.css";

function UploadPage() {
    const navigate = useNavigate();

    const [reqFile, setReqFile] = useState(null);
    const [subFile, setSubFile] = useState(null);
    const [reqError, setReqError] = useState(false);
    const [subError, setSubError] = useState(false);
    const [reqDrag, setReqDrag] = useState(false);
    const [subDrag, setSubDrag] = useState(false);
    const [loading, setLoading] = useState(false);

    const allowed = ["pdf", "docx", "txt"];

    const validateFile = (file, type) => {
        const ext = file.name.split(".").pop().toLowerCase();
        const valid = allowed.includes(ext);

        if (!valid) {
            if (type === "req") setReqError(true);
            else setSubError(true);
            return false;
        }

        if (type === "req") {
            setReqFile(file);
            setReqError(false);
        } else {
            setSubFile(file);
            setSubError(false);
        }
        return true;
    };

    const handleAnalyze = () => {
        if (!reqFile || !subFile) return;

        setLoading(true);
        setTimeout(() => {
            setLoading(false);
            navigate("/analyze", {
                state: {
                    requirementsFile: reqFile,
                    submissionFile: subFile
                }
            });
        }, 300);
    };

    return (
        <>
            {loading && (
                <div className="loading-overlay">
                    <div className="loading-box">
                        <div className="spinner"></div>
                        <p className="loading-text">분석 준비 중...</p>
                    </div>
                </div>
            )}

            <nav className="navbar">
                <div
                    className="nav-left"
                    onClick={() => navigate("/")}
                    style={{cursor: "pointer"}}
                >
                    <img
                        src={logo}
                        alt="logo"
                        style={{height: "110px"}}
                    />
                </div>
            </nav>

            <div className="hero-section">
                <div className="hero-title"><img
                    src={logo_main}
                    alt="logo_main"
                    style={{height: "140px"}}
                />
                </div>
                <p className="hero-subtitle">
                    제출할 문서를 요구사항과 비교하여 충족 여부를 분석합니다.
                </p>
                <div className="hero-full-divider"/>
            </div>

            <div className="upload-wrapper">

                <div className="upload-container">

                    <div className="upload-card">
                        <div className="card-title">📘 요구사항 문서</div>

                        <div
                            className={`dropzone 
                                ${reqError ? "dropzone-error" : ""} 
                                ${reqDrag ? "drag-active" : ""}`}
                            onDragOver={(e) => e.preventDefault()}
                            onDragEnter={() => setReqDrag(true)}
                            onDragLeave={() => setReqDrag(false)}
                            onDrop={(e) => {
                                e.preventDefault();
                                setReqDrag(false);
                                validateFile(e.dataTransfer.files[0], "req");
                            }}
                        >
                            <input
                                type="file"
                                accept=".pdf,.docx,.txt"
                                className="file-input"
                                onChange={(e) => validateFile(e.target.files[0], "req")}
                            />

                            {reqFile ? (
                                <span className="file-name">{reqFile.name}</span>
                            ) : (
                                <span className="placeholder">여기에 파일을 드래그하거나 클릭하세요</span>
                            )}
                        </div>

                        {reqError && (
                            <div className="error-text">
                                지원하지 않는 파일 형식입니다.
                            </div>
                        )}

                        <div className="file-hint">
                            • 지원 형식: PDF, DOCX, TXT
                        </div>
                    </div>

                    {/* SUBMISSION */}
                    <div className="upload-card">
                        <div className="card-title">📗 제출 문서</div>

                        <div
                            className={`dropzone 
                                ${subError ? "dropzone-error" : ""}
                                ${subDrag ? "drag-active" : ""}`}
                            onDragOver={(e) => e.preventDefault()}
                            onDragEnter={() => setSubDrag(true)}
                            onDragLeave={() => setSubDrag(false)}
                            onDrop={(e) => {
                                e.preventDefault();
                                setSubDrag(false);
                                validateFile(e.dataTransfer.files[0], "sub");
                            }}
                        >
                            <input
                                type="file"
                                accept=".pdf,.docx,.txt"
                                className="file-input"
                                onChange={(e) => validateFile(e.target.files[0], "sub")}
                            />

                            {subFile ? (
                                <span className="file-name">{subFile.name}</span>
                            ) : (
                                <span className="placeholder">여기에 파일을 드래그하거나 클릭하세요</span>
                            )}
                        </div>

                        {subError && (
                            <div className="error-text">
                                지원하지 않는 파일 형식입니다.
                            </div>
                        )}

                        <div className="file-hint">
                            • 지원 형식: PDF, DOCX, TXT
                        </div>
                    </div>
                </div>

                {/* BUTTON */}
                <button
                    className="analyze-button"
                    disabled={!reqFile || !subFile || loading}
                    onClick={handleAnalyze}
                >
                    분석하기
                </button>

            </div>

            {/* FOOTER */}
            <footer className="footer">
                <div className="footer-inner">
                    <div className="footer-col">
                        <h4>CheckMate</h4>
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
        </>
    );
}

export default UploadPage;
