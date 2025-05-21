import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const SettingsPage: React.FC = () => {
  const [theme, setTheme] = useState('system');
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [pushNotifications, setPushNotifications] = useState(false);

  return (
    <div style={{ maxWidth: '600px', margin: 'auto', padding: '40px', fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '10px' }}>Settings</h1>
      <p style={{ marginBottom: '30px' }}>Here you can configure your preferences.</p>



      <hr style={{ margin: '30px 0' }} />

      <section>
        <h2 style={{ fontSize: '1.2rem' }}>Appearance</h2>
        <label htmlFor="theme-select" style={{ display: 'block', marginBottom: '8px' }}>Theme:</label>
        <select
          id="theme-select"
          name="theme"
          value={theme}
          onChange={(e) => setTheme(e.target.value)}
          style={{ padding: '5px', fontSize: '1rem' }}
        >
          <option value="light">Light</option>
          <option value="dark">Dark</option>
          <option value="system">System</option>
        </select>
      </section>

      <hr style={{ margin: '30px 0' }} />

      <section>
        <h2 style={{ fontSize: '1.2rem' }}>Notifications</h2>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="checkbox"
            id="email-notifications"
            checked={emailNotifications}
            onChange={(e) => setEmailNotifications(e.target.checked)}
          />
          <label htmlFor="email-notifications" style={{ marginLeft: '8px' }}>Enable email notifications</label>
        </div>
        <div>
          <input
            type="checkbox"
            id="push-notifications"
            checked={pushNotifications}
            onChange={(e) => setPushNotifications(e.target.checked)}
          />
          <label htmlFor="push-notifications" style={{ marginLeft: '8px' }}>Enable push notifications</label>
        </div>
      </section>

      <div style={{ marginTop: '40px' }}>
        <Link to="/" style={{ textDecoration: 'underline' }}>← Back to Homepage</Link>
      </div>
    </div>
  );
};

export default SettingsPage;
