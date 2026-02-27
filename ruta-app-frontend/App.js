import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  FlatList,
  Alert,
  KeyboardAvoidingView,
  Platform,
  StatusBar,
  Dimensions
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { User, Mail, Lock, Phone, Trash2, LogOut, UserPlus, LogIn } from 'lucide-react-native';

const { width } = Dimensions.get('window');
const API_URL = 'http://192.168.1.4:3000'; // IP local detectada

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nombre, setNombre] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [users, setUsers] = useState([]);

  const handleLogin = async () => {
    if (!email || !password) return Alert.alert('Campos requeridos', 'Por favor completa todos los campos');
    try {
      const response = await fetch(`${API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correo: email, password }),
      });
      const data = await response.json();
      if (response.ok) {
        setIsLoggedIn(true);
        fetchUsers();
      } else {
        Alert.alert('Error de Acceso', data.error);
      }
    } catch (error) {
      Alert.alert('Error de Conexión', 'Asegúrate de que el servidor backend esté encendido.');
    }
  };

  const handleRegister = async () => {
    if (!nombre || !email || !password) return Alert.alert('Campos requeridos', 'Por favor completa todos los campos');
    try {
      const response = await fetch(`${API_URL}/usuarios`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre, correo: email, password }),
      });
      const data = await response.json();
      if (response.ok) {
        Alert.alert('¡Bienvenido!', 'Usuario registrado correctamente');
        setIsRegistering(false);
      } else {
        Alert.alert('Error de Registro', data.error);
      }
    } catch (error) {
      Alert.alert('Error de Conexión', 'No se pudo conectar con el servidor.');
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await fetch(`${API_URL}/usuarios`);
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error(error);
    }
  };

  const deleteUser = async (id) => {
    Alert.confirm ? Alert.confirm('Confirmar', '¿Estás seguro de eliminar este usuario?') : null;
    try {
      const response = await fetch(`${API_URL}/usuarios/${id}`, { method: 'DELETE' });
      if (response.ok) {
        Alert.alert('Éxito', 'Usuario eliminado');
        fetchUsers();
      }
    } catch (error) {
      Alert.alert('Error', 'No se pudo eliminar el usuario');
    }
  };

  if (!isLoggedIn) {
    return (
      <LinearGradient colors={['#4c669f', '#3b5998', '#192f6a']} style={styles.gradient}>
        <StatusBar barStyle="light-content" />
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.container}>
          <View style={styles.authCard}>
            <Text style={styles.title}>{isRegistering ? 'Crear Cuenta' : 'Trekking App'}</Text>
            <Text style={styles.subtitle}>{isRegistering ? 'Únete a la aventura' : 'Bienvenido de nuevo'}</Text>

            {isRegistering && (
              <View style={styles.inputContainer}>
                <User color="#fff" size={20} style={styles.icon} />
                <TextInput
                  style={styles.input}
                  placeholder="Nombre Completo"
                  placeholderTextColor="#rgba(255,255,255,0.6)"
                  value={nombre}
                  onChangeText={setNombre}
                />
              </View>
            )}

            <View style={styles.inputContainer}>
              <Mail color="#fff" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Correo Electrónico"
                placeholderTextColor="#rgba(255,255,255,0.6)"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
              />
            </View>

            <View style={styles.inputContainer}>
              <Lock color="#fff" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Contraseña"
                placeholderTextColor="#rgba(255,255,255,0.6)"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
              />
            </View>

            <TouchableOpacity
              style={styles.primaryButton}
              onPress={isRegistering ? handleRegister : handleLogin}
            >
              <Text style={styles.buttonText}>{isRegistering ? 'REGISTRARSE' : 'INICIAR SESIÓN'}</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.secondaryButton}
              onPress={() => setIsRegistering(!isRegistering)}
            >
              <Text style={styles.secondaryButtonText}>
                {isRegistering ? '¿Ya tienes cuenta? Inicia Sesión' : '¿No tienes cuenta? Regístrate'}
              </Text>
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      </LinearGradient>
    );
  }

  return (
    <View style={styles.mainContainer}>
      <StatusBar barStyle="dark-content" />
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Exploradores</Text>
        <TouchableOpacity onPress={() => setIsLoggedIn(false)}>
          <LogOut color="#ff4444" size={24} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={users}
        keyExtractor={(item) => item.idusuario.toString()}
        contentContainerStyle={styles.listContent}
        renderItem={({ item }) => (
          <View style={styles.userCard}>
            <View style={styles.userInfo}>
              <View style={styles.avatarPlaceholder}>
                <Text style={styles.avatarText}>{item.nombre[0].toUpperCase()}</Text>
              </View>
              <View>
                <Text style={styles.userName}>{item.nombre}</Text>
                <Text style={styles.userEmail}>{item.correo}</Text>
              </View>
            </View>
            <TouchableOpacity onPress={() => deleteUser(item.idusuario)}>
              <Trash2 color="#ff4444" size={20} />
            </TouchableOpacity>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  gradient: {
    flex: 1,
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  authCard: {
    width: '100%',
    maxWidth: 400,
    padding: 32,
    borderRadius: 28,
    backgroundColor: 'rgba(255, 255, 255, 0.12)',
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.25)',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 10,
  },
  title: {
    fontSize: 36,
    fontWeight: '800',
    color: '#fff',
    marginBottom: 8,
    textAlign: 'center',
    textShadowColor: 'rgba(0, 0, 0, 0.3)',
    textShadowOffset: { width: 0, height: 2 },
    textShadowRadius: 4,
  },
  subtitle: {
    fontSize: 18,
    color: 'rgba(255, 255, 255, 0.85)',
    marginBottom: 40,
    textAlign: 'center',
    fontWeight: '400',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    height: 60,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    marginBottom: 16,
    paddingHorizontal: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.15)',
  },
  icon: {
    marginRight: 12,
  },
  input: {
    flex: 1,
    color: '#fff',
    fontSize: 17,
    fontWeight: '500',
  },
  primaryButton: {
    width: '100%',
    height: 60,
    backgroundColor: '#fff',
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 8,
  },
  buttonText: {
    color: '#192f6a',
    fontSize: 18,
    fontWeight: '700',
    letterSpacing: 1.5,
  },
  secondaryButton: {
    marginTop: 25,
    padding: 10,
  },
  secondaryButtonText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '600',
    textDecorationLine: 'underline',
    opacity: 0.9,
  },
  // Main App Styles
  mainContainer: {
    flex: 1,
    backgroundColor: '#F0F4F8',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingTop: 65,
    paddingBottom: 25,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#E1E8ED',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 3,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: '800',
    color: '#1A2b4c',
  },
  listContent: {
    padding: 20,
  },
  userCard: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 20,
    marginBottom: 18,
    alignItems: 'center',
    justifyContent: 'space-between',
    shadowColor: '#1A2b4c',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 4,
    borderWidth: 1,
    borderColor: '#F8FAFC',
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  avatarPlaceholder: {
    width: 52,
    height: 52,
    borderRadius: 18,
    backgroundColor: '#3b5998',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 18,
    shadowColor: '#3b5998',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
  },
  avatarText: {
    color: '#fff',
    fontWeight: '800',
    fontSize: 22,
  },
  userName: {
    fontSize: 18,
    fontWeight: '700',
    color: '#334155',
  },
  userEmail: {
    fontSize: 14,
    color: '#64748B',
    marginTop: 2,
  },
  deleteButton: {
    padding: 12,
    backgroundColor: 'rgba(255, 68, 68, 0.08)',
    borderRadius: 12,
  }
});
