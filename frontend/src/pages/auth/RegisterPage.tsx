import { useState } from 'react';
import { useAuth, UserProfile } from '../../context/AuthContext';
import { api } from '../../services/api';
import { Card, Input, Button } from '../../components/ui';
import { Eye, EyeOff, Mail, Lock, User } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';

interface RegisterResponse {
  accessToken: string;
  refreshToken: string;
  userLogin: UserProfile;
}

export function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await api.post<any, RegisterResponse>('/register', { 
        name, 
        email, 
        password 
      });
      
      // Lưu token và thông tin user
      login(response.accessToken, response.refreshToken, response.userLogin);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Đã có lỗi xảy ra. Email có thể đã được sử dụng.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Tạo tài khoản</h1>
          <p className="text-gray-500 dark:text-gray-400">Bắt đầu hành trình học tập của bạn</p>
        </div>

        {error && (
          <div className="mb-6 p-3 bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-sm rounded-lg text-center">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <User className="w-5 h-5 text-gray-400" />
              </div>
              <Input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="pl-10 w-full"
                placeholder="Họ và tên"
                required
              />
            </div>
          </div>

          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail className="w-5 h-5 text-gray-400" />
              </div>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10 w-full"
                placeholder="Địa chỉ Email"
                required
              />
            </div>
          </div>

          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock className="w-5 h-5 text-gray-400" />
              </div>
              <Input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 pr-10 w-full"
                placeholder="Mật khẩu"
                required
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
          </div>

          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock className="w-5 h-5 text-gray-400" />
              </div>
              <Input
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="pl-10 pr-10 w-full"
                placeholder="Xác nhận mật khẩu"
                required
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
          </div>

          <Button type="submit" className="w-full mt-6 py-2.5 bg-primary-600 hover:bg-primary-700 text-white rounded-lg transition-colors" disabled={isLoading}>
            {isLoading ? 'Đang tạo...' : 'Tạo tài khoản'}
          </Button>
        </form>

        <div className="mt-8 text-center text-sm text-gray-600 dark:text-gray-400">
          Đã có tài khoản?{' '}
          <Link to="/login" className="font-semibold text-primary-600 hover:text-primary-700">
            Đăng nhập ngay
          </Link>
        </div>
      </Card>
    </div>
  );
}
