const { app, BrowserWindow, dialog } = require('electron')
const path = require('path')
const { spawn } = require('child_process')
const fs = require('fs')
const http = require('http')

let mainWindow
let backendProcess

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })

  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:3000')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../frontend/dist/index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function waitForBackend(maxAttempts = 30, interval = 1000) {
  return new Promise((resolve, reject) => {
    let attempts = 0
    const check = () => {
      attempts++
      http.get('http://localhost:8080/api/templates', (res) => {
        if (res.statusCode === 200) {
          resolve()
        } else if (attempts < maxAttempts) {
          setTimeout(check, interval)
        } else {
          reject(new Error('Backend startup timeout'))
        }
      }).on('error', () => {
        if (attempts < maxAttempts) {
          setTimeout(check, interval)
        } else {
          reject(new Error('Backend startup timeout'))
        }
      })
    }
    check()
  })
}

function startBackend() {
  const backendPath = path.join(__dirname, '../backend/target/api-pressure-test-tool-1.0.0.jar')

  if (!fs.existsSync(backendPath)) {
    dialog.showErrorBox('错误', '找不到后端程序，请先运行 npm run build 构建项目')
    app.quit()
    return false
  }

  backendProcess = spawn('java', ['-jar', backendPath], {
    stdio: 'inherit',
  })

  backendProcess.on('error', (err) => {
    console.error('启动后端失败:', err)
    dialog.showErrorBox('错误', '启动后端失败: ' + err.message)
  })

  backendProcess.on('exit', (code) => {
    console.log('后端进程退出:', code)
  })

  return true
}

function killBackend() {
  if (backendProcess && !backendProcess.killed) {
    backendProcess.kill()
  }
}

app.whenReady().then(async () => {
  if (!startBackend()) {
    return
  }

  try {
    await waitForBackend()
    createWindow()
  } catch (err) {
    dialog.showErrorBox('错误', '后端启动超时，请检查Java环境')
    app.quit()
  }

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  killBackend()
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  killBackend()
})
