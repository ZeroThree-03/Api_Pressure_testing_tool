export class WebSocketClient {
  constructor(url) {
    this.url = url
    this.ws = null
    this.callbacks = {}
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.intentionalClose = false
  }

  connect() {
    this.intentionalClose = false
    this.ws = new WebSocket(this.url)

    this.ws.onopen = () => {
      this.reconnectAttempts = 0
      this.emit('open')
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.emit(data.type, data.data)
      } catch (e) {
        this.emit('error', e)
      }
    }

    this.ws.onclose = () => {
      this.emit('close')
      if (!this.intentionalClose) {
        this.tryReconnect()
      }
    }

    this.ws.onerror = (error) => {
      this.emit('error', error)
    }
  }

  tryReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
      setTimeout(() => this.connect(), delay)
    }
  }

  on(event, callback) {
    if (!this.callbacks[event]) {
      this.callbacks[event] = []
    }
    this.callbacks[event].push(callback)
  }

  off(event, callback) {
    if (this.callbacks[event]) {
      this.callbacks[event] = this.callbacks[event].filter(cb => cb !== callback)
    }
  }

  emit(event, data) {
    if (this.callbacks[event]) {
      this.callbacks[event].forEach(cb => cb(data))
    }
  }

  close() {
    this.intentionalClose = true
    if (this.ws) {
      this.ws.close()
    }
  }
}
