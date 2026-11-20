let currentMode = 'match';
let globalVocabs = [];

// ==================== CHUYỂN ĐỔI CHẾ ĐỘ ====================
function switchGameMode(mode) {
    currentMode = mode;
    document.querySelectorAll('.nav-pills .nav-link').forEach(btn => {
        btn.classList.remove('active', 'text-white');
        btn.classList.add('text-dark');
    });
    document.querySelectorAll('.game-container').forEach(c => c.classList.add('d-none'));

    if (mode === 'match') {
        document.getElementById('tab-match').classList.add('active', 'text-white');
        document.getElementById('mode-match').classList.remove('d-none');
        startMatchGame();
    } else if (mode === 'scramble') {
        document.getElementById('tab-scramble').classList.add('active', 'text-white');
        document.getElementById('mode-scramble').classList.remove('d-none');
        startScrambleGame();
    } else if (mode === 'listen') {
        document.getElementById('tab-listen').classList.add('active', 'text-white');
        document.getElementById('mode-listen').classList.remove('d-none');
        startListenGame();
    }
}

function speak(text) {
    if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'en-US';
        window.speechSynthesis.speak(utterance);
    }
}

// ==================== MODE 1: SPEED MATCH ====================
let matchCards = [], selectedMatchCards = [], matchedPairs = 0, timerInterval = null, startTime = null;

async function startMatchGame() {
    clearInterval(timerInterval);
    document.getElementById("win-banner").classList.add("d-none");
    matchedPairs = 0;
    selectedMatchCards = [];

    const board = document.getElementById("game-board");
    board.innerHTML = `<div class="text-center w-100 my-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Đang tải dữ liệu...</div>`;

    try {
        const res = await fetch('/api/vocabularies/game?limit=6');
        const data = await res.json();
        const list = data.map(item => ({ en: item.word, vi: item.meaning, phienAm: item.phienAm || '' }));

        document.getElementById("total-count").innerText = list.length;
        document.getElementById("matched-count").innerText = "0";
        document.getElementById("timer").innerText = "0.0s";

        matchCards = [];
        list.forEach((item, idx) => {
            matchCards.push({ id: idx, type: 'en', text: item.en, phienAm: item.phienAm, matchVal: item.en });
            matchCards.push({ id: idx, type: 'vi', text: item.vi, phienAm: '', matchVal: item.en });
        });
        matchCards.sort(() => Math.random() - 0.5);

        board.innerHTML = "";
        matchCards.forEach((card, idx) => {
            const el = document.createElement("div");
            el.className = "word-card";
            el.innerHTML = card.type === 'en' && card.phienAm ? `<div><div>${card.text}</div><small class="text-muted fw-normal" style="font-size:0.8rem;">${card.phienAm}</small></div>` : card.text;
            el.onclick = () => handleMatchClick(idx, el);
            board.appendChild(el);
        });

        startTime = Date.now();
        timerInterval = setInterval(() => {
            document.getElementById("timer").innerText = ((Date.now() - startTime) / 1000).toFixed(1) + "s";
        }, 100);
    } catch (e) {
        board.innerHTML = `<div class="alert alert-danger w-100 text-center">Không thể kết nối CSDL!</div>`;
    }
}

function handleMatchClick(idx, el) {
    const card = matchCards[idx];
    if (selectedMatchCards.some(c => c.idx === idx) || el.classList.contains("matched")) return;
    if (card.type === 'en') speak(card.text);

    el.classList.add("selected");
    selectedMatchCards.push({ idx, data: card, element: el });

    if (selectedMatchCards.length === 2) {
        const [c1, c2] = selectedMatchCards;
        if (c1.data.type !== c2.data.type && c1.data.matchVal === c2.data.matchVal) {
            setTimeout(() => {
                c1.element.classList.add("matched");
                c2.element.classList.add("matched");
                selectedMatchCards = [];
                matchedPairs++;
                document.getElementById("matched-count").innerText = matchedPairs;
                if (matchedPairs === matchCards.length / 2) {
                    clearInterval(timerInterval);
                    document.getElementById("final-time").innerText = document.getElementById("timer").innerText;
                    document.getElementById("win-banner").classList.remove("d-none");
                }
            }, 200);
        } else {
            setTimeout(() => { c1.element.classList.add("wrong"); c2.element.classList.add("wrong"); }, 150);
            setTimeout(() => {
                c1.element.classList.remove("selected", "wrong");
                c2.element.classList.remove("selected", "wrong");
                selectedMatchCards = [];
            }, 600);
        }
    }
}

// ==================== MODE 2: WORD SCRAMBLE ====================
let scrambleIndex = 0, scrambleScore = 0, currentAnswerLetters = [];

async function startScrambleGame() {
    scrambleIndex = 0;
    scrambleScore = 0;
    document.getElementById("scramble-score").innerText = "0";

    const res = await fetch('/api/vocabularies/game?limit=5');
    globalVocabs = await res.json();
    document.getElementById("scramble-total").innerText = globalVocabs.length;
    renderScrambleRound();
}

function renderScrambleRound() {
    if (scrambleIndex >= globalVocabs.length) {
        alert("🎉 Chúc mừng bạn đã hoàn thành bộ xếp chữ với " + scrambleScore + " điểm!");
        startScrambleGame();
        return;
    }

    const item = globalVocabs[scrambleIndex];
    document.getElementById("scramble-index").innerText = scrambleIndex + 1;
    document.getElementById("scramble-meaning").innerText = item.meaning;
    document.getElementById("scramble-phienam").innerText = item.phienAm || '';

    currentAnswerLetters = [];
    document.getElementById("scramble-answer").innerHTML = "";

    const letters = item.word.replace(/\s+/g, '').toUpperCase().split('').sort(() => Math.random() - 0.5);
    const letterBox = document.getElementById("scramble-letters");
    letterBox.innerHTML = "";

    letters.forEach((char) => {
        const btn = document.createElement("button");
        btn.className = "btn btn-outline-primary letter-btn shadow-sm";
        btn.innerText = char;
        btn.onclick = () => {
            currentAnswerLetters.push(char);
            btn.disabled = true;
            renderAnswerLetters();
            checkScrambleComplete();
        };
        letterBox.appendChild(btn);
    });
}

function renderAnswerLetters() {
    const ansBox = document.getElementById("scramble-answer");
    ansBox.innerHTML = "";
    currentAnswerLetters.forEach(char => {
        const box = document.createElement("span");
        box.className = "badge bg-primary fs-4 m-1 px-3 py-2";
        box.innerText = char;
        ansBox.appendChild(box);
    });
}

function resetCurrentScramble() {
    currentAnswerLetters = [];
    renderAnswerLetters();
    document.querySelectorAll('#scramble-letters button').forEach(b => b.disabled = false);
}

function checkScrambleComplete() {
    const targetWord = globalVocabs[scrambleIndex].word.replace(/\s+/g, '').toUpperCase();
    if (currentAnswerLetters.length === targetWord.length) {
        if (currentAnswerLetters.join('') === targetWord) {
            speak(globalVocabs[scrambleIndex].word);
            scrambleScore += 10;
            document.getElementById("scramble-score").innerText = scrambleScore;
            setTimeout(() => {
                scrambleIndex++;
                renderScrambleRound();
            }, 600);
        } else {
            alert("Chưa chính xác, thử lại nhé!");
            resetCurrentScramble();
        }
    }
}

function nextScrambleQuestion() {
    scrambleIndex++;
    renderScrambleRound();
}

// ==================== MODE 3: LISTENING CHALLENGE ====================
let listenIndex = 0, listenScore = 0;

async function startListenGame() {
    listenIndex = 0;
    listenScore = 0;
    document.getElementById("listen-score").innerText = "0";

    const res = await fetch('/api/vocabularies/game?limit=5');
    globalVocabs = await res.json();
    document.getElementById("listen-total").innerText = globalVocabs.length;
    renderListenRound();
}

function renderListenRound() {
    if (listenIndex >= globalVocabs.length) {
        alert("🎉 Hoàn thành phần thi Nghe chép từ! Điểm: " + listenScore);
        startListenGame();
        return;
    }

    const item = globalVocabs[listenIndex];
    document.getElementById("listen-index").innerText = listenIndex + 1;
    document.getElementById("listen-hint").innerText = "Gợi ý nghĩa: " + item.meaning;
    document.getElementById("listen-input").value = "";
    document.getElementById("listen-feedback").innerText = "";
    playCurrentListenAudio();
}

function playCurrentListenAudio() {
    const item = globalVocabs[listenIndex];
    speak(item.word);
}

function checkListeningAnswer() {
    const input = document.getElementById("listen-input").value.trim().toLowerCase();
    const target = globalVocabs[listenIndex].word.trim().toLowerCase();
    const fb = document.getElementById("listen-feedback");

    if (input === target) {
        fb.className = "fw-bold text-success mb-3";
        fb.innerText = " Chính xác! +10 Điểm";
        listenScore += 10;
        document.getElementById("listen-score").innerText = listenScore;
        setTimeout(() => {
            listenIndex++;
            renderListenRound();
        }, 800);
    } else {
        fb.className = "fw-bold text-danger mb-3";
        fb.innerText = ` Sai rồi! Đáp án đúng là: ${globalVocabs[listenIndex].word}`;
    }
}

// Chạy mặc định chế độ Speed Match khi mở trang
document.addEventListener("DOMContentLoaded", () => startMatchGame());