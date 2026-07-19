import React, { useEffect, useState } from 'react';
import { devopsApi } from '../services/devopsApi';
import type { PhaseDetailResponse, PhaseRequest } from '../types/devops-dynamic.types';
import type { CurriculumItem, ToolItem, LearningStep, QuizQuestion, PracticeLab } from '../types/devops.types';
import {
  ShieldAlert, Plus, Edit2, Trash2, CheckCircle2, AlertCircle,
  BookOpen, Wrench, Route, HelpCircle, Code2, Layers, Save, X
} from 'lucide-react';

/**
 * TRANG QUẢN TRỊ NỘI DUNG DEVOPS LIFECYCLE (ADMIN CONTENT CRUD DASHBOARD)
 *
 * Tuân thủ yêu cầu bắt buộc:
 * 1. "ok code có comment và chỉ có admin mới thêm sửa xóa đc": Chỉ ROLE_ADMIN mới được phép
 *    thực hiện Thêm mới, Chỉnh sửa, và Xóa nội dung của từng giai đoạn trong vòng đời DevOps.
 * 2. "bỏ phần terminal và các lệnh như git add, git push, ... và thêm sử xóa phần nội dung
 *    của từng phần trong devopslifecycle": Đã loại bỏ hoàn toàn kịch bản Terminal giả lập,
 *    thay vào đó cho phép Admin Thêm / Sửa / Xóa trực tiếp cấu trúc bài học thực tế:
 *    - Thông tin chung & Hero Banner (tagline, summary, heroSnippet)
 *    - Chương trình học (Curriculum)
 *    - Công cụ hỗ trợ (Tools)
 *    - Lộ trình 5 bước (Learning Path)
 *    - Câu hỏi trắc nghiệm (Quiz)
 *    - Bài tập thực hành Lab & Code snippet (Practice Labs)
 */
export const DevOpsAdminPage: React.FC = () => {
  const [phases, setPhases] = useState<PhaseDetailResponse[]>([]);
  const [selectedPhase, setSelectedPhase] = useState<PhaseDetailResponse | null>(null);
  const [activeContentTab, setActiveContentTab] = useState<'general' | 'curriculum' | 'tools' | 'learningPath' | 'quiz' | 'labs'>('general');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Form states cho Phase được chọn
  const [formData, setFormData] = useState<PhaseRequest>({
    phaseKey: '',
    title: '',
    name: '',
    tagline: '',
    summary: '',
    heroSnippetTitle: '',
    heroSnippet: '',
    iconName: 'code-2',
    colorGradient: 'from-blue-500 to-cyan-600',
    displayOrder: 1,
    active: true,
    theme: {
      gradient: 'from-blue-500 to-cyan-600',
      iconBg: 'bg-blue-500/10',
      badgeBg: 'bg-blue-500/20',
      badgeText: 'text-blue-300',
      borderColor: 'border-blue-500/30',
      accentColor: 'text-blue-400',
      ctaBg: 'bg-blue-600',
      ctaText: 'text-white'
    },
    curriculum: [],
    tools: [],
    learningPath: [],
    quiz: [],
    handsOnLabs: []
  });

  const [isCreatingPhase, setIsCreatingPhase] = useState<boolean>(false);

  // Tải danh sách Phase kèm nội dung khi mở trang
  const fetchPhases = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await devopsApi.getAllPhasesForAdmin();
      setPhases(data);
      if (data.length > 0 && !selectedPhase) {
        handleSelectPhase(data[0]);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Không thể tải danh sách giai đoạn DevOps. Vui lòng kiểm tra quyền Admin.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPhases();
  }, []);

  // Chọn một giai đoạn để chỉnh sửa
  const handleSelectPhase = (phase: PhaseDetailResponse) => {
    setSelectedPhase(phase);
    setIsCreatingPhase(false);
    setFormData({
      phaseKey: phase.phaseKey || '',
      title: phase.title || '',
      name: phase.name || phase.title || '',
      tagline: phase.tagline || '',
      summary: phase.summary || '',
      heroSnippetTitle: phase.heroSnippetTitle || '',
      heroSnippet: phase.heroSnippet || '',
      iconName: phase.iconName || 'code-2',
      colorGradient: phase.colorGradient || 'from-blue-500 to-cyan-600',
      displayOrder: phase.displayOrder || 1,
      active: phase.active ?? true,
      theme: phase.theme || {
        gradient: phase.colorGradient || 'from-blue-500 to-cyan-600',
        iconBg: 'bg-blue-500/10',
        badgeBg: 'bg-blue-500/20',
        badgeText: 'text-blue-300',
        borderColor: 'border-blue-500/30',
        accentColor: 'text-blue-400',
        ctaBg: 'bg-blue-600',
        ctaText: 'text-white'
      },
      curriculum: Array.isArray(phase.curriculum) ? phase.curriculum : [],
      tools: Array.isArray(phase.tools) ? phase.tools : [],
      learningPath: Array.isArray(phase.learningPath) ? phase.learningPath : [],
      quiz: Array.isArray(phase.quiz) ? phase.quiz : [],
      handsOnLabs: Array.isArray(phase.handsOnLabs) ? phase.handsOnLabs : []
    });
  };

  // Mở form tạo Giai đoạn mới
  const handleStartCreatePhase = () => {
    setSelectedPhase(null);
    setIsCreatingPhase(true);
    setActiveContentTab('general');
    setFormData({
      phaseKey: '',
      title: '',
      name: '',
      tagline: '',
      summary: '',
      heroSnippetTitle: '',
      heroSnippet: '',
      iconName: 'clipboard-list',
      colorGradient: 'from-blue-500 to-cyan-600',
      displayOrder: phases.length + 1,
      active: true,
      theme: {
        gradient: 'from-blue-500 to-cyan-600',
        iconBg: 'bg-blue-500/10',
        badgeBg: 'bg-blue-500/20',
        badgeText: 'text-blue-300',
        borderColor: 'border-blue-500/30',
        accentColor: 'text-blue-400',
        ctaBg: 'bg-blue-600',
        ctaText: 'text-white'
      },
      curriculum: [],
      tools: [],
      learningPath: [],
      quiz: [],
      handsOnLabs: []
    });
  };

  // Lưu thao tác Thêm hoặc Sửa Phase xuống DB
  const handleSavePhase = async () => {
    setError(null);
    setSuccessMsg(null);
    try {
      if (isCreatingPhase) {
        const created = await devopsApi.createPhase(formData);
        setSuccessMsg(`🎉 Tạo thành công giai đoạn "${created.title}" với đầy đủ cấu trúc nội dung!`);
        await fetchPhases();
        handleSelectPhase(created);
      } else if (selectedPhase && selectedPhase.id) {
        const updated = await devopsApi.updatePhase(selectedPhase.id, formData);
        setSuccessMsg(`✅ Đã cập nhật nội dung giai đoạn "${updated.title}" vào Database thành công!`);
        await fetchPhases();
        handleSelectPhase(updated);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Lỗi khi lưu dữ liệu Phase. Vui lòng kiểm tra lại quyền Admin hoặc thông tin.');
    }
  };

  // Xóa mềm / Ẩn Phase
  const handleDeletePhase = async (id: number, title: string) => {
    if (!window.confirm(`Bạn có chắc muốn xóa/ẩn giai đoạn "${title}" khỏi hệ thống?`)) return;
    setError(null);
    try {
      await devopsApi.deletePhase(id);
      setSuccessMsg(`🗑️ Đã ẩn giai đoạn "${title}".`);
      await fetchPhases();
      if (selectedPhase?.id === id) {
        setSelectedPhase(null);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Không thể xóa giai đoạn này.');
    }
  };

  // ==========================================================================
  // CÁC HÀM CRUD CHI TIẾT TỪNG PHẦN NỘI DUNG (CURRICULUM, TOOLS, QUIZ, LABS...)
  // ==========================================================================

  // 1. Curriculum CRUD
  const handleAddCurriculum = () => {
    const newItem: CurriculumItem = {
      id: `curr-${Date.now()}`,
      title: 'Bài học mới về DevOps',
      category: 'Core Fundamentals',
      duration: '45 phút',
      level: 'Intermediate',
      description: 'Mô tả chi tiết nội dung kiến thức cốt lõi và mục tiêu đạt được...',
      tags: ['DevOps', 'Best Practices'],
      objectives: ['Nắm vững lý thuyết', 'Áp dụng vào thực tiễn']
    };
    setFormData(prev => ({ ...prev, curriculum: [...(prev.curriculum || []), newItem] }));
  };

  const handleUpdateCurriculum = (idx: number, field: keyof CurriculumItem, value: any) => {
    setFormData(prev => {
      const list = [...(prev.curriculum || [])];
      list[idx] = { ...list[idx], [field]: value };
      return { ...prev, curriculum: list };
    });
  };

  const handleDeleteCurriculum = (idx: number) => {
    setFormData(prev => ({
      ...prev,
      curriculum: (prev.curriculum || []).filter((_, i) => i !== idx)
    }));
  };

  // 2. Tools CRUD
  const handleAddTool = () => {
    const newTool: ToolItem = {
      name: 'Công cụ mới',
      category: 'CI/CD',
      description: 'Mô tả vai trò và ứng dụng thực tế của công cụ này trong hệ thống...',
      industryStandard: true,
      documentationUrl: 'https://devops.com'
    };
    setFormData(prev => ({ ...prev, tools: [...(prev.tools || []), newTool] }));
  };

  const handleUpdateTool = (idx: number, field: keyof ToolItem, value: any) => {
    setFormData(prev => {
      const list = [...(prev.tools || [])];
      list[idx] = { ...list[idx], [field]: value };
      return { ...prev, tools: list };
    });
  };

  const handleDeleteTool = (idx: number) => {
    setFormData(prev => ({
      ...prev,
      tools: (prev.tools || []).filter((_, i) => i !== idx)
    }));
  };

  // 3. Learning Path CRUD
  const handleAddLearningStep = () => {
    const newStep: LearningStep = {
      stepNumber: (formData.learningPath?.length || 0) + 1,
      title: 'Bước mới: Triển khai & Kiểm thử',
      duration: '30 phút',
      category: 'Core Fundamentals',
      description: 'Hướng dẫn chi tiết thao tác từng bước trên hệ thống thực tế...',
      keyTakeaway: 'Sẵn sàng tích hợp vào quy trình CI/CD'
    };
    setFormData(prev => ({ ...prev, learningPath: [...(prev.learningPath || []), newStep] }));
  };

  const handleUpdateLearningStep = (idx: number, field: keyof LearningStep, value: any) => {
    setFormData(prev => {
      const list = [...(prev.learningPath || [])];
      list[idx] = { ...list[idx], [field]: value };
      return { ...prev, learningPath: list };
    });
  };

  const handleDeleteLearningStep = (idx: number) => {
    setFormData(prev => ({
      ...prev,
      learningPath: (prev.learningPath || []).filter((_, i) => i !== idx)
    }));
  };

  // 4. Quiz CRUD
  const handleAddQuiz = () => {
    const newQuiz: QuizQuestion = {
      question: 'Câu hỏi kiểm tra kiến thức mới về giai đoạn này là gì?',
      options: ['Đáp án A đúng', 'Đáp án B sai', 'Đáp án C sai', 'Đáp án D sai'],
      correctIndex: 0,
      explanation: 'Giải thích lý do tại sao chọn đáp án A và các nguyên tắc liên quan.',
      difficulty: 'Intermediate'
    };
    setFormData(prev => ({ ...prev, quiz: [...(prev.quiz || []), newQuiz] }));
  };

  const handleUpdateQuiz = (idx: number, field: keyof QuizQuestion, value: any) => {
    setFormData(prev => {
      const list = [...(prev.quiz || [])];
      list[idx] = { ...list[idx], [field]: value };
      return { ...prev, quiz: list };
    });
  };

  const handleDeleteQuiz = (idx: number) => {
    setFormData(prev => ({
      ...prev,
      quiz: (prev.quiz || []).filter((_, i) => i !== idx)
    }));
  };

  // 5. Practice Labs CRUD
  const handleAddLab = () => {
    const newLab: PracticeLab = {
      id: `lab-${Date.now()}`,
      title: 'Bài thực hành Lab mới',
      tabTitle: `Lab ${(formData.handsOnLabs?.length || 0) + 1}`,
      level: 'Intermediate',
      duration: '60 phút',
      difficulty: 'Vừa phải',
      prerequisites: 'Đã cài đặt Git, Docker và có tài khoản GitHub',
      desc: 'Hướng dẫn thực hành chi tiết cấu hình file YAML/Code và chạy thử nghiệm...',
      objectives: ['Hoàn thành cấu hình file', 'Kiểm tra hoạt động thành công'],
      codeSnippet: '# Cấu hình YAML hoặc Code mẫu cho Lab\nversion: 1.0\nname: devops-practice-lab',
      snippetLabel: 'Cấu hình mẫu Lab'
    };
    setFormData(prev => ({ ...prev, handsOnLabs: [...(prev.handsOnLabs || []), newLab] }));
  };

  const handleUpdateLab = (idx: number, field: keyof PracticeLab, value: any) => {
    setFormData(prev => {
      const list = [...(prev.handsOnLabs || [])];
      list[idx] = { ...list[idx], [field]: value };
      return { ...prev, handsOnLabs: list };
    });
  };

  const handleDeleteLab = (idx: number) => {
    setFormData(prev => ({
      ...prev,
      handsOnLabs: (prev.handsOnLabs || []).filter((_, i) => i !== idx)
    }));
  };

  return (
    <div className="space-y-6 text-gray-900 dark:text-gray-100 font-sans pb-16">
      {/* Header Ban Quản trị */}
      <div className="bg-gradient-to-r from-amber-500/10 via-amber-500/5 to-transparent border border-amber-200 dark:border-amber-500/30 rounded-2xl p-6 flex flex-wrap items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center gap-4">
          <div className="p-3.5 bg-amber-500/10 dark:bg-amber-500/20 rounded-2xl border border-amber-300/60 dark:border-amber-500/40 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0 shadow-xs">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <h1 className="text-xl lg:text-2xl font-bold text-gray-900 dark:text-gray-100 tracking-tight">QUẢN TRỊ NỘI DUNG DEVOPS LIFECYCLE</h1>
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider bg-amber-100 dark:bg-amber-500/20 text-amber-800 dark:text-amber-300 border border-amber-200 dark:border-amber-500/30">
                ROLE_ADMIN ONLY
              </span>
            </div>
            <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
              Chỉ tài khoản <strong className="text-amber-700 dark:text-amber-400 font-semibold">ADMIN</strong> mới có quyền Thêm, Sửa, Xóa nội dung (Curriculum, Tools, Quiz, Labs...) trực tiếp trong Database MySQL.
            </p>
          </div>
        </div>
        <button
          onClick={handleStartCreatePhase}
          className="flex items-center gap-2 bg-gradient-to-r from-primary-600 to-secondary-600 hover:from-primary-700 hover:to-secondary-700 text-white font-semibold px-5 py-2.5 rounded-xl transition-all shadow-sm hover:shadow-md shrink-0"
        >
          <Plus className="w-5 h-5 stroke-[2.5]" /> Thêm Giai Đoạn Mới
        </button>
      </div>

      {/* Thông báo lỗi / thành công */}
      {error && (
        <div className="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-500/30 text-rose-800 dark:text-rose-200 p-4 rounded-xl flex items-center justify-between shadow-sm">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-rose-500 dark:text-rose-400 shrink-0" />
            <span className="text-sm font-semibold">{error}</span>
          </div>
          <button onClick={() => setError(null)} className="text-gray-400 hover:text-gray-600 dark:hover:text-white"><X className="w-4 h-4" /></button>
        </div>
      )}

      {successMsg && (
        <div className="bg-emerald-50 dark:bg-emerald-500/10 border border-emerald-200 dark:border-emerald-500/30 text-emerald-800 dark:text-emerald-200 p-4 rounded-xl flex items-center justify-between shadow-sm animate-fadeIn">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-500 dark:text-emerald-400 shrink-0" />
            <span className="text-sm font-semibold">{successMsg}</span>
          </div>
          <button onClick={() => setSuccessMsg(null)} className="text-gray-400 hover:text-gray-600 dark:hover:text-white"><X className="w-4 h-4" /></button>
        </div>
      )}

      {/* Bố cục 2 cột: Sidebar chọn Phase (Trái) & Editor nội dung (Phải) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Cột Trái: Danh sách 8 giai đoạn */}
        <div className="lg:col-span-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-5 shadow-sm space-y-4">
          <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 px-1">
            <span>Danh Sách Giai Đoạn ({phases.length})</span>
            <span className="text-primary-600 dark:text-primary-400 font-mono">TL: 1-8</span>
          </div>

          {loading ? (
            <div className="bg-gray-50 dark:bg-gray-900/50 border border-gray-200 dark:border-gray-700 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">
              Đang tải dữ liệu từ Java Backend...
            </div>
          ) : phases.length === 0 ? (
            <div className="bg-gray-50 dark:bg-gray-900/50 border border-gray-200 dark:border-gray-700 rounded-xl p-6 text-center text-gray-500 dark:text-gray-400 text-sm">
              Chưa có giai đoạn nào. Bấm nút Thêm Giai Đoạn Mới ở trên!
            </div>
          ) : (
            <div className="space-y-2.5 max-h-[720px] overflow-y-auto pr-1">
              {phases.map((phase) => {
                const isSelected = selectedPhase?.id === phase.id && !isCreatingPhase;
                return (
                  <div
                    key={phase.id}
                    onClick={() => handleSelectPhase(phase)}
                    className={`p-4 rounded-xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${
                      isSelected
                        ? 'bg-primary-50 dark:bg-primary-900/30 border-primary-500 text-primary-950 dark:text-primary-100 shadow-sm ring-1 ring-primary-500/20'
                        : 'bg-gray-50 dark:bg-gray-900/40 border-gray-200 dark:border-gray-700 hover:border-primary-300 dark:hover:border-primary-700 hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-800 dark:text-gray-200'
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <span className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold font-mono shrink-0 ${
                        isSelected
                          ? 'bg-primary-600 text-white shadow-xs'
                          : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
                      }`}>
                        #{phase.displayOrder}
                      </span>
                      <div className="min-w-0">
                        <div className="font-bold text-gray-900 dark:text-gray-100 flex items-center gap-2 truncate">
                          <span className="truncate">{phase.title}</span>
                          {!phase.active && (
                            <span className="px-1.5 py-0.5 rounded bg-rose-100 dark:bg-rose-500/20 text-rose-700 dark:text-rose-300 text-[10px] font-bold shrink-0">Ẩn</span>
                          )}
                        </div>
                        <div className="text-xs text-gray-500 dark:text-gray-400 font-mono mt-0.5 truncate">slug: /{phase.phaseKey}</div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 shrink-0" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => handleSelectPhase(phase)}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/40 transition-all"
                        title="Chỉnh sửa nội dung"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDeletePhase(phase.id!, phase.title)}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/30 transition-all"
                        title="Xóa / Ẩn giai đoạn"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Cột Phải: Trình chỉnh sửa Nội dung theo Tab (Curriculum, Tools, Quiz...) */}
        <div className="lg:col-span-8 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 shadow-sm flex flex-col min-h-[680px]">
          {isCreatingPhase || selectedPhase ? (
            <div className="space-y-6 flex-1 flex flex-col">
              {/* Header Editor */}
              <div className="flex flex-wrap items-center justify-between border-b border-gray-200 dark:border-gray-700 pb-4 gap-4">
                <div>
                  <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100 flex items-center gap-2">
                    {isCreatingPhase ? '🚀 Thêm Giai Đoạn Mới' : `📝 Sửa Nội Dung: ${formData.title} (${formData.phaseKey})`}
                  </h2>
                  <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                    Mọi chỉnh sửa bên dưới sẽ được lưu trực tiếp vào bảng <code className="text-primary-600 dark:text-primary-400 font-mono font-semibold">devops_phases</code> trong MySQL và phản ánh ngay lập tức trên trang học viên.
                  </p>
                </div>
                <button
                  onClick={handleSavePhase}
                  className="flex items-center gap-2 bg-success-600 hover:bg-success-700 text-white font-semibold px-5 py-2.5 rounded-xl shadow-sm hover:shadow transition-all shrink-0"
                >
                  <Save className="w-4 h-4" /> Lưu Thay Đổi
                </button>
              </div>

              {/* Thanh chọn Tab Nội Dung */}
              <div className="flex items-center gap-1.5 overflow-x-auto pb-2 border-b border-gray-200 dark:border-gray-700 scrollbar-hide pt-1">
                {[
                  { id: 'general', label: '1. Thông tin & Hero', icon: Layers, count: null },
                  { id: 'curriculum', label: '2. Chương Trình Học', icon: BookOpen, count: formData.curriculum?.length || 0 },
                  { id: 'tools', label: '3. Tools & Ecosystem', icon: Wrench, count: formData.tools?.length || 0 },
                  { id: 'learningPath', label: '4. Lộ Trình 5 Bước', icon: Route, count: formData.learningPath?.length || 0 },
                  { id: 'quiz', label: '5. Câu Hỏi Trắc Nghiệm', icon: HelpCircle, count: formData.quiz?.length || 0 },
                  { id: 'labs', label: '6. Thực Hành Labs', icon: Code2, count: formData.handsOnLabs?.length || 0 },
                ].map(tab => {
                  const Icon = tab.icon;
                  const isActive = activeContentTab === tab.id;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveContentTab(tab.id as any)}
                      className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 whitespace-nowrap ${
                        isActive
                          ? 'bg-primary-600 text-white shadow-xs ring-1 ring-primary-500'
                          : 'bg-gray-100 dark:bg-gray-700/60 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-gray-100'
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                      <span>{tab.label}</span>
                      {tab.count !== null && (
                        <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-mono font-semibold ${isActive ? 'bg-black/20 text-white' : 'bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-300'}`}>
                          {tab.count}
                        </span>
                      )}
                    </button>
                  );
                })}
              </div>

              {/* NỘI DUNG CHI TIẾT CỦA TỪNG TAB */}
              <div className="flex-1 overflow-y-auto pr-1 space-y-5 max-h-[600px]">
                
                {/* TAB 1: THÔNG TIN CHUNG & HERO BANNER */}
                {activeContentTab === 'general' && (
                  <div className="space-y-4 font-sans text-sm">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Khóa URL (Slug - không dấu): <span className="text-rose-500">*</span></label>
                        <input
                          type="text"
                          value={formData.phaseKey}
                          onChange={e => setFormData({ ...formData, phaseKey: e.target.value.toLowerCase().replace(/\s+/g, '-') })}
                          placeholder="VD: plan, code, build..."
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 font-mono text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Tên ngắn hiển thị (Title): <span className="text-rose-500">*</span></label>
                        <input
                          type="text"
                          value={formData.title}
                          onChange={e => setFormData({ ...formData, title: e.target.value, name: formData.name || e.target.value })}
                          placeholder="VD: Code, Build, Deploy..."
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Tên đầy đủ (Name trên Hero Banner):</label>
                        <input
                          type="text"
                          value={formData.name}
                          onChange={e => setFormData({ ...formData, name: e.target.value })}
                          placeholder="VD: Collaborative Coding & Git Version Control"
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Thứ tự Timeline (1-8): <span className="text-rose-500">*</span></label>
                        <input
                          type="number"
                          value={formData.displayOrder}
                          onChange={e => setFormData({ ...formData, displayOrder: parseInt(e.target.value) || 1 })}
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 font-mono text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Câu Slogan / Tagline (Hero Subtitle):</label>
                      <input
                        type="text"
                        value={formData.tagline}
                        onChange={e => setFormData({ ...formData, tagline: e.target.value })}
                        placeholder="VD: Viết mã sạch, kiểm soát phiên bản Git & quản lý luồng làm việc nhóm..."
                        className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Bài tóm tắt chi tiết giai đoạn (Summary):</label>
                      <textarea
                        rows={3}
                        value={formData.summary}
                        onChange={e => setFormData({ ...formData, summary: e.target.value })}
                        placeholder="Mô tả ý nghĩa và vai trò của giai đoạn trong toàn bộ quy trình DevOps..."
                        className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-3.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                      />
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Tiêu đề đoạn mẫu Code/YAML Banner:</label>
                        <input
                          type="text"
                          value={formData.heroSnippetTitle}
                          onChange={e => setFormData({ ...formData, heroSnippetTitle: e.target.value })}
                          placeholder="VD: Quy trình GitFlow & Pull Request"
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Icon (lucide-react):</label>
                        <input
                          type="text"
                          value={formData.iconName}
                          onChange={e => setFormData({ ...formData, iconName: e.target.value })}
                          className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl px-3.5 py-2.5 text-gray-900 dark:text-gray-100 font-mono text-sm focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Nội dung đoạn Code / YAML mẫu (Hero Snippet):</label>
                      <textarea
                        rows={4}
                        value={formData.heroSnippet}
                        onChange={e => setFormData({ ...formData, heroSnippet: e.target.value })}
                        placeholder="Nhập nội dung code YAML hoặc shell script hiển thị trên banner trang chi tiết..."
                        className="w-full bg-gray-900 dark:bg-gray-950 border border-gray-700 rounded-xl p-3.5 text-emerald-400 font-mono text-xs focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all shadow-xs"
                      />
                    </div>
                  </div>
                )}

                {/* TAB 2: CHƯƠNG TRÌNH HỌC (CURRICULUM CRUD) */}
                {activeContentTab === 'curriculum' && (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">Danh sách các bài học lý thuyết & thực hành trong chương trình</span>
                      <button
                        onClick={handleAddCurriculum}
                        className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-xs font-bold px-3.5 py-2 rounded-xl transition-all shadow-xs shrink-0"
                      >
                        <Plus className="w-4 h-4" /> Thêm Bài Học
                      </button>
                    </div>

                    {(formData.curriculum || []).map((item, idx) => (
                      <div key={item.id || idx} className="bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl p-4 space-y-3 relative transition-all hover:border-gray-300 dark:hover:border-gray-600">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold font-mono text-amber-600 dark:text-amber-400">Bài #{idx + 1} ({item.category})</span>
                          <button
                            onClick={() => handleDeleteCurriculum(idx)}
                            className="text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 p-1 rounded-lg transition-all"
                            title="Xóa bài học này"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                          <div className="sm:col-span-2">
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Tiêu đề bài học:</label>
                            <input
                              type="text"
                              value={item.title}
                              onChange={e => handleUpdateCurriculum(idx, 'title', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Thời lượng:</label>
                            <input
                              type="text"
                              value={item.duration}
                              onChange={e => handleUpdateCurriculum(idx, 'duration', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Phân loại:</label>
                            <select
                              value={item.category}
                              onChange={e => handleUpdateCurriculum(idx, 'category', e.target.value as any)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            >
                              <option value="Core Fundamentals">Core Fundamentals</option>
                              <option value="Advanced Practices">Advanced Practices</option>
                            </select>
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Cấp độ:</label>
                            <select
                              value={item.level}
                              onChange={e => handleUpdateCurriculum(idx, 'level', e.target.value as any)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            >
                              <option value="Beginner">Beginner</option>
                              <option value="Intermediate">Intermediate</option>
                              <option value="Advanced">Advanced</option>
                              <option value="Enterprise">Enterprise</option>
                            </select>
                          </div>
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Mô tả bài học:</label>
                          <textarea
                            rows={2}
                            value={item.description}
                            onChange={e => handleUpdateCurriculum(idx, 'description', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* TAB 3: TOOLS & ECOSYSTEM CRUD */}
                {activeContentTab === 'tools' && (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">Danh sách công cụ chuẩn công nghiệp trong hệ sinh thái giai đoạn này</span>
                      <button
                        onClick={handleAddTool}
                        className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-xs font-bold px-3.5 py-2 rounded-xl transition-all shadow-xs shrink-0"
                      >
                        <Plus className="w-4 h-4" /> Thêm Công Cụ
                      </button>
                    </div>

                    {(formData.tools || []).map((tool, idx) => (
                      <div key={idx} className="bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl p-4 space-y-3 relative transition-all hover:border-gray-300 dark:hover:border-gray-600">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold font-mono text-cyan-600 dark:text-cyan-400">Tool #{idx + 1} ({tool.category})</span>
                          <button
                            onClick={() => handleDeleteTool(idx)}
                            className="text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 p-1 rounded-lg transition-all"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Tên phần mềm / công cụ:</label>
                            <input
                              type="text"
                              value={tool.name}
                              onChange={e => handleUpdateTool(idx, 'name', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Nhóm (Category):</label>
                            <select
                              value={tool.category}
                              onChange={e => handleUpdateTool(idx, 'category', e.target.value as any)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            >
                              <option value="Version Control">Version Control</option>
                              <option value="CI/CD">CI/CD</option>
                              <option value="Testing & QA">Testing & QA</option>
                              <option value="Artifacts & Containers">Artifacts & Containers</option>
                              <option value="Orchestration & IaC">Orchestration & IaC</option>
                              <option value="Observability & SRE">Observability & SRE</option>
                              <option value="Agile Planning">Agile Planning</option>
                              <option value="Security">Security</option>
                            </select>
                          </div>
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Mô tả vai trò trong quy trình:</label>
                          <textarea
                            rows={2}
                            value={tool.description}
                            onChange={e => handleUpdateTool(idx, 'description', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* TAB 4: LỘ TRÌNH 5 BƯỚC (LEARNING PATH CRUD) */}
                {activeContentTab === 'learningPath' && (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">Các bước tuần tự để học viên làm chủ hoàn toàn giai đoạn này</span>
                      <button
                        onClick={handleAddLearningStep}
                        className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-xs font-bold px-3.5 py-2 rounded-xl transition-all shadow-xs shrink-0"
                      >
                        <Plus className="w-4 h-4" /> Thêm Bước Học
                      </button>
                    </div>

                    {(formData.learningPath || []).map((step, idx) => (
                      <div key={idx} className="bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl p-4 space-y-3 relative transition-all hover:border-gray-300 dark:hover:border-gray-600">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold font-mono text-purple-600 dark:text-purple-400">Bước #{step.stepNumber}: {step.title} ({step.category})</span>
                          <button
                            onClick={() => handleDeleteLearningStep(idx)}
                            className="text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 p-1 rounded-lg transition-all"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                          <div className="sm:col-span-2">
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Tiêu đề bước:</label>
                            <input
                              type="text"
                              value={step.title}
                              onChange={e => handleUpdateLearningStep(idx, 'title', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Thời gian (phút):</label>
                            <input
                              type="text"
                              value={step.duration}
                              onChange={e => handleUpdateLearningStep(idx, 'duration', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Phân loại:</label>
                            <select
                              value={step.category}
                              onChange={e => handleUpdateLearningStep(idx, 'category', e.target.value as any)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            >
                              <option value="Core Fundamentals">Core Fundamentals</option>
                              <option value="Advanced Practices">Advanced Practices</option>
                            </select>
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Điểm mấu chốt (Key Takeaway):</label>
                            <input
                              type="text"
                              value={step.keyTakeaway || ''}
                              onChange={e => handleUpdateLearningStep(idx, 'keyTakeaway', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Hướng dẫn chi tiết bước này:</label>
                          <textarea
                            rows={2}
                            value={step.description}
                            onChange={e => handleUpdateLearningStep(idx, 'description', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* TAB 5: CÂU HỎI TRẮC NGHIỆM (QUIZ CRUD) */}
                {activeContentTab === 'quiz' && (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">Ngân hàng câu hỏi trắc nghiệm củng cố kiến thức cho học viên</span>
                      <button
                        onClick={handleAddQuiz}
                        className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-xs font-bold px-3.5 py-2 rounded-xl transition-all shadow-xs shrink-0"
                      >
                        <Plus className="w-4 h-4" /> Thêm Câu Hỏi
                      </button>
                    </div>

                    {(formData.quiz || []).map((q, idx) => (
                      <div key={idx} className="bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl p-4 space-y-3 relative transition-all hover:border-gray-300 dark:hover:border-gray-600">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold font-mono text-emerald-600 dark:text-emerald-400">Câu hỏi #{idx + 1} ({q.difficulty})</span>
                          <button
                            onClick={() => handleDeleteQuiz(idx)}
                            className="text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 p-1 rounded-lg transition-all"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Nội dung câu hỏi:</label>
                          <input
                            type="text"
                            value={q.question}
                            onChange={e => handleUpdateQuiz(idx, 'question', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400">Các đáp án lựa chọn (Chọn đáp án đúng bằng nút tròn bên trái):</label>
                          {(q.options || []).map((opt, optIdx) => (
                            <div key={optIdx} className="flex items-center gap-2.5">
                              <input
                                type="radio"
                                name={`correct-${idx}`}
                                checked={q.correctIndex === optIdx}
                                onChange={() => handleUpdateQuiz(idx, 'correctIndex', optIdx)}
                                className="w-4 h-4 accent-emerald-600 cursor-pointer shrink-0"
                              />
                              <input
                                type="text"
                                value={opt}
                                onChange={e => {
                                  const newOpts = [...(q.options || [])];
                                  newOpts[optIdx] = e.target.value;
                                  handleUpdateQuiz(idx, 'options', newOpts);
                                }}
                                placeholder={`Đáp án ${String.fromCharCode(65 + optIdx)}...`}
                                className={`w-full bg-white dark:bg-gray-900 border rounded-lg px-3 py-1.5 text-xs text-gray-900 dark:text-gray-100 focus:outline-none transition-all shadow-2xs ${
                                  q.correctIndex === optIdx ? 'border-emerald-500 bg-emerald-50/50 dark:bg-emerald-500/10 font-semibold' : 'border-gray-200 dark:border-gray-700'
                                }`}
                              />
                            </div>
                          ))}
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Giải thích chi tiết cho câu hỏi:</label>
                          <input
                            type="text"
                            value={q.explanation}
                            onChange={e => handleUpdateQuiz(idx, 'explanation', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-xs text-gray-700 dark:text-gray-300 focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* TAB 6: THỰC HÀNH LABS & CODE SNIPPET (LABS CRUD) */}
                {activeContentTab === 'labs' && (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">Các bài tập Lab thực tế kèm cấu trúc Code/YAML mẫu</span>
                      <button
                        onClick={handleAddLab}
                        className="flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-xs font-bold px-3.5 py-2 rounded-xl transition-all shadow-xs shrink-0"
                      >
                        <Plus className="w-4 h-4" /> Thêm Bài Lab
                      </button>
                    </div>

                    {(formData.handsOnLabs || []).map((lab, idx) => (
                      <div key={idx} className="bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl p-4 space-y-3 relative transition-all hover:border-gray-300 dark:hover:border-gray-600">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold font-mono text-amber-600 dark:text-amber-400">Lab #{idx + 1}: {lab.title} ({lab.level})</span>
                          <button
                            onClick={() => handleDeleteLab(idx)}
                            className="text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 p-1 rounded-lg transition-all"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Tên bài Lab:</label>
                            <input
                              type="text"
                              value={lab.title}
                              onChange={e => handleUpdateLab(idx, 'title', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                          <div>
                            <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Tên Tab hiển thị (VD: Lab 1, Lab 2):</label>
                            <input
                              type="text"
                              value={lab.tabTitle}
                              onChange={e => handleUpdateLab(idx, 'tabTitle', e.target.value)}
                              className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-1.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                            />
                          </div>
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Mô tả bài Lab:</label>
                          <textarea
                            rows={2}
                            value={lab.desc}
                            onChange={e => handleUpdateLab(idx, 'desc', e.target.value)}
                            className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-2.5 text-gray-900 dark:text-gray-100 text-sm focus:border-primary-500 focus:outline-none shadow-2xs"
                          />
                        </div>

                        <div>
                          <label className="block text-[11px] font-bold text-gray-600 dark:text-gray-400 mb-1">Đoạn Code / YAML mẫu cho Lab ({lab.snippetLabel || 'Snippet'}):</label>
                          <textarea
                            rows={4}
                            value={lab.codeSnippet || ''}
                            onChange={e => handleUpdateLab(idx, 'codeSnippet', e.target.value)}
                            placeholder="Nhập code/YAML thực hành cho học viên..."
                            className="w-full bg-gray-900 dark:bg-gray-950 border border-gray-700 rounded-lg p-3 text-emerald-400 font-mono text-xs focus:border-primary-500 focus:outline-none transition-all shadow-2xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-center text-gray-400 p-8">
              <BookOpen className="w-16 h-16 text-gray-300 dark:text-gray-600 mb-4 stroke-1" />
              <h3 className="text-lg font-bold text-gray-700 dark:text-gray-300 mb-1">Chọn một Giai đoạn ở cột bên trái</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 max-w-md">
                Bạn có thể Thêm, Sửa, hoặc Xóa nội dung chi tiết của từng phần (Curriculum, Tools, Quiz, Labs...) trong vòng đời DevOps Lifecycle.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
