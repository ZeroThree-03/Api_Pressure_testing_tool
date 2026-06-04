const { app, BrowserWindow, dialog, Menu } = require('electron')
const path = require('path')
const { spawn } = require('child_process')
const fs = require('fs')
const http = require('http')

let mainWindow
let backendProcess

function createMenu() {
  const template = [
    {
      label: '文件',
      submenu: [
        { label: '新建窗口', accelerator: 'CmdOrCtrl+N', click: () => createWindow() },
        { type: 'separator' },
        { label: '退出', accelerator: 'CmdOrCtrl+Q', click: () => app.quit() },
      ],
    },
    {
      label: '编辑',
      submenu: [
        { label: '撤销', accelerator: 'CmdOrCtrl+Z', role: 'undo' },
        { label: '重做', accelerator: 'Shift+CmdOrCtrl+Z', role: 'redo' },
        { type: 'separator' },
        { label: '剪切', accelerator: 'CmdOrCtrl+X', role: 'cut' },
        { label: '复制', accelerator: 'CmdOrCtrl+C', role: 'copy' },
        { label: '粘贴', accelerator: 'CmdOrCtrl+V', role: 'paste' },
        { label: '全选', accelerator: 'CmdOrCtrl+A', role: 'selectAll' },
      ],
    },
    {
      label: '视图',
      submenu: [
        { label: '重新加载', accelerator: 'CmdOrCtrl+R', role: 'reload' },
        { label: '强制重新加载', accelerator: 'CmdOrCtrl+Shift+R', role: 'forceReload' },
        { label: '开发者工具', accelerator: 'F12', role: 'toggleDevTools' },
        { type: 'separator' },
        { label: '实际大小', accelerator: 'CmdOrCtrl+0', role: 'resetZoom' },
        { label: '放大', accelerator: 'CmdOrCtrl+=', role: 'zoomIn' },
        { label: '缩小', accelerator: 'CmdOrCtrl+-', role: 'zoomOut' },
        { type: 'separator' },
        { label: '全屏', accelerator: 'F11', role: 'togglefullscreen' },
      ],
    },
    {
      label: '窗口',
      submenu: [
        { label: '最小化', role: 'minimize' },
        { label: '关闭', role: 'close' },
      ],
    },
    {
      label: '帮助',
      submenu: [
        {
          label: '关于',
          click: () => {
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: '关于',
              message: 'API压测工具',
              detail: '版本 1.0.0\n一款基于curl命令的API压力测试工具',
            })
          },
        },
      ],
    },
  ]

  const menu = Menu.buildFromTemplate(template)
  Menu.setApplicationMenu(menu)
}

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

  // Always open DevTools for debugging
  mainWindow.webContents.openDevTools()

  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
  } else {
    const indexPath = path.join(__dirname, '../frontend/dist/index.html')
    console.log('Loading index from:', indexPath)
    console.log('File exists:', fs.existsSync(indexPath))
    mainWindow.loadFile(indexPath)
  }

  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.error('Failed to load:', errorCode, errorDescription)
  })

  mainWindow.webContents.on('console-message', (event, level, message) => {
    console.log('Renderer:', message)
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function waitForBackend(maxAttempts = 60, interval = 1000) {
  return new Promise((resolve, reject) => {
    let attempts = 0
    const check = () => {
      attempts++
      http.get('http://localhost:8080/api/templates', (res) => {
        if (res.statusCode === 200) {
          console.log('Backend is ready!')
          resolve()
        } else if (attempts < maxAttempts) {
          setTimeout(check, interval)
        } else {
          reject(new Error('Backend startup timeout'))
        }
      }).on('error', () => {
        if (attempts < maxAttempts) {
          if (attempts % 5 === 0) {
            console.log(`Waiting for backend... (${attempts}/${maxAttempts})`)
          }
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
  const isPackaged = app.isPackaged
  const baseDir = isPackaged
    ? path.join(__dirname, '../../app.asar.unpacked')
    : path.join(__dirname, '..')

  const backendPath = path.join(baseDir, 'backend/target/api-pressure-test-0.0.1-SNAPSHOT.jar')
  console.log('Backend JAR path:', backendPath)
  console.log('JAR exists:', fs.existsSync(backendPath))

  if (!fs.existsSync(backendPath)) {
    dialog.showErrorBox('错误', '找不到后端程序: ' + backendPath)
    app.quit()
    return false
  }

  // Set working directory for SQLite database
  const workDir = isPackaged
    ? path.join(app.getPath('userData'))
    : path.join(__dirname, '..')

  // Create backend/data directory if not exists
  const dataDir = path.join(workDir, 'backend/data')
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true })
  }

  // Copy SQLite DB to userData if not exists
  if (isPackaged) {
    const dbSource = path.join(baseDir, 'backend/data/pressurtest.db')
    const dbTarget = path.join(dataDir, 'pressurtest.db')
    if (fs.existsSync(dbSource) && !fs.existsSync(dbTarget)) {
      fs.copyFileSync(dbSource, dbTarget)
      console.log('Copied database to:', dbTarget)
    }
  }

  console.log('Starting backend with cwd:', workDir)
  console.log('Java command: java -jar', backendPath)

  // Find Java executable
  const javaHome = process.env.JAVA_HOME
  const javaCmd = javaHome ? path.join(javaHome, 'bin', 'java') : 'java'
  console.log('Using Java:', javaCmd)

  backendProcess = spawn(javaCmd, ['-jar', backendPath], {
    cwd: workDir,
    stdio: ['ignore', 'pipe', 'pipe'],
  })

  backendProcess.stdout.on('data', (data) => {
    console.log('Backend stdout:', data.toString())
  })

  backendProcess.stderr.on('data', (data) => {
    console.log('Backend stderr:', data.toString())
  })

  backendProcess.on('error', (err) => {
    console.error('Failed to start backend:', err)
    dialog.showErrorBox('错误', '启动后端失败: ' + err.message)
  })

  backendProcess.on('exit', (code) => {
    console.log('Backend process exited with code:', code)
  })

  return true
}

function killBackend() {
  if (backendProcess && !backendProcess.killed) {
    console.log('Killing backend process')
    backendProcess.kill()
  }
}

app.whenReady().then(async () => {
  console.log('App is ready, starting backend...')
  createMenu()

  if (!startBackend()) {
    return
  }

  try {
    await waitForBackend()
    console.log('Backend ready, creating window...')
    createWindow()
  } catch (err) {
    console.error('Backend startup failed:', err)
    dialog.showErrorBox('错误', '后端启动超时，请检查Java环境\n\n' + err.message)
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
